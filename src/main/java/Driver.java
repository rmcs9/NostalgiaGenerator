import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.util.LinkedList;
import java.sql.*;


public class Driver {
    /*
    todo instead of running these through commmand line args, use userconfigs.json
    args[0] = discord bot token
    args[1] = db url
    args[2] = db username
    args[3] = db pass
     */
    public static void main(String[] args) throws Exception{
        Connection dbConnect;
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
           throw e;
        }
        try{
            //todo edit here to remove the hardcoded parameters
            dbConnect = DriverManager.getConnection(args[1], args[2], args[3]);
        }
        catch(SQLException e){
           throw e;
        }

        DiscordClient client = DiscordClient.create(args[0]);
        GatewayDiscordClient gatewayDiscordClientMono = client.login().block();

        LinkedList<String> commandFiles = new LinkedList<>();
        commandFiles.add("cooldown.json");
        commandFiles.add("cooldownlist.json");
        commandFiles.add("rmvcooldown.json");

        new GlobalCommandRegistrar(gatewayDiscordClientMono.getRestClient()).registerCommands(commandFiles);

        Mono<Void> newMSG = client.withGateway((GatewayDiscordClient gateway) -> {
            Mono<Void> onMessage = gateway.on(MessageCreateEvent.class, event -> {
                Message message = event.getMessage();
                if (message.getAuthor().isPresent()) {
                    if (!message.getAuthor().get().isBot()) {
                       //todo logic for msg actions and cooldowns
                    }
                }
                return Mono.empty();
            }).then();

            Mono<Void> onSlash = gateway.on(ChatInputInteractionEvent.class, event -> {
                //todo logic for slash commands
                if(event.getCommandName().equals("oncooldown")){

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
