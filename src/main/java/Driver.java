import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.sql.*;
import java.util.Scanner;


public class Driver {
    public static void main(String[] args) throws Exception{
        String botToken;
        String db_url;
        String db_username;
        String db_password;
        try{
            BufferedReader userConfigsReader = new BufferedReader(new FileReader("src/main/resources/userconfigs.json"));
            JsonReader userConfigReader = Json.createReader(userConfigsReader);
            JsonObject userConfig = userConfigReader.readObject();

            botToken = userConfig.getString("token");

            JsonObject dbinfo = userConfig.getJsonObject("db");
            db_url = dbinfo.getString("URL");
            db_username = dbinfo.getString("username");
            db_password = dbinfo.getString("pass");
        }
        catch (Exception e){
            Scanner input = new Scanner(System.in);
            if(e instanceof FileNotFoundException){
                System.out.println("userconfigs file not present at src/main/resources/");
            }
            else{
                System.out.println("userconfigs.json failed to parse");
            }
            System.out.println("Please enter necessary startup details manually\n");
            System.out.println("enter your discord provided bot token: ");
            botToken = input.nextLine();

            System.out.println("enter your database url: ");
            db_url = input.nextLine();

            System.out.println("enter your mysql username: ");
            db_username = input.nextLine();

            System.out.println("enter your mysql password: ");
            db_password = input.nextLine();
        }



        Connection dbConnect;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
           throw e;
        }
        try{
            dbConnect = DriverManager.getConnection(db_url, db_username, db_password);
        }
        catch(SQLException e){
           throw e;
        }


        DiscordClient client = DiscordClient.create(botToken);
        GatewayDiscordClient gatewayDiscordClientMono = client.login().block();

        LinkedList<String> commandFiles = new LinkedList<>();
        commandFiles.add("cooldown.json");
        commandFiles.add("cooldownlist.json");
        commandFiles.add("rmvcooldown.json");
//        commandFiles.add("addlistitem.json");
//        commandFiles.add("info.json");
//        commandFiles.add("showList.json");

        new GlobalCommandRegistrar(gatewayDiscordClientMono.getRestClient()).registerCommands(commandFiles);

        Cooldown cooldown = new Cooldown(dbConnect);

        Mono<Void> newMSG = client.withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> onMessage = gateway.on(MessageCreateEvent.class, event -> {
                Message message = event.getMessage();
                if (message.getAuthor().isPresent()) {
                    if (!message.getAuthor().get().isBot()) {
                        //todo logic for messages
                    }
                }
                return Mono.empty();
            }).then();

            Mono<Void> onSlash = gateway.on(ChatInputInteractionEvent.class, event -> {
                //todo logic for slash commands
                if(event.getCommandName().equals("oncooldown")){
                    try {
                        return SlashHandlers.handleOnCooldown(event, dbConnect);
                    } catch (SQLException e) {
                        return event.reply("slash command failed. database error");
                    }
                }
                else if(event.getCommandName().equals("cooldownlist")){

                }
                else if(event.getCommandName().equals("rmvcooldown")){

                }
                return Mono.empty();
            }).then();

            return onMessage.and(onSlash);
        });

        newMSG.block();

    }
}
