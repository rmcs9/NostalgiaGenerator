import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SlashHandlers {
    public static Mono<Void> handleOnCooldown(ChatInputInteractionEvent event, Connection db) throws SQLException {
        String authorID = event.getInteraction().getUser().getTag();
        String authorMention = event.getInteraction().getUser().getMention();
        PreparedStatement query = db.prepareStatement("SELECT * FROM onCooldown WHERE discord_id = ?");
        query.setString(1, authorID);

        ResultSet results = query.executeQuery();

        if (results.next()) {
            Timestamp expire = results.getTimestamp(2);
            long minLeft = Instant.now().until(Instant.ofEpochMilli(expire.getTime()), ChronoUnit.MINUTES);
            return event.reply("user " + authorMention + " has " + minLeft + " minutes left on cooldown.");
        } else {
            return event.reply("user " + authorMention + " is not currently on cooldown.");
        }
    }
}
