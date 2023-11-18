import java.sql.*;
import java.util.Calendar;
import java.util.HashMap;


public class Cooldown {
    /*
    INSTANT CLASS

    Instant.now() - obtain current instant time from the system clock
    Instant.toString() - sends back a string in the format of  2023-11-18T00:00:27.337585Z
    Instant.parse() - will create a new instance object based on its toString format


    ACTUALLY INSTEAD OF USING CALENDAR SHIT USE java.sql.Timestamp
     */

    //todo implement cooldown primer
    private HashMap<String, Calendar> primer;

    private final Connection db;

    public Cooldown(Connection data){
        primer = new HashMap<>();
        db = data;
    }
    public boolean addCooldown(String discID) throws SQLException {
        Calendar calExpire = Calendar.getInstance();
        calExpire.add(Calendar.MINUTE, 10);

        Timestamp expire = Timestamp.from(calExpire.toInstant());

        PreparedStatement statement = db.prepareStatement("INSERT INTO onCooldown VALUES (?, ?)");
        statement.setString(1, discID);
        statement.setTimestamp(2, expire);

        statement.execute();
        return true;
    }

    public int refreshCooldown() throws SQLException {
        PreparedStatement removes = db.prepareStatement("DELETE FROM onCooldown WHERE cooldown_expire < CURRENT_TIMESTAMP");
        return removes.executeUpdate();
    }
}
