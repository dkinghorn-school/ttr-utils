package edu.goldenhammer.database.postgresql.data_types;

/**
 * Created by seanjib on 2/3/2017.
 */
public class SQLPlayer {
    public static final String ID = "user_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String TABLE_NAME = "player";
    public static final String CREATE_STMT = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (\n" +
                    "    %2$s SERIAL UNIQUE,\n" +
                    "    %3$s VARCHAR(20) UNIQUE NOT NULL,\n" +
                    "    %4$s VARCHAR(20) NOT NULL,\n" +
                    "    %5$s VARCHAR(20) UNIQUE,\n" +
                    "    PRIMARY KEY(%6$s)\n" +
                    ");"
            , TABLE_NAME, ID, USERNAME, PASSWORD, ACCESS_TOKEN, ID);


    public SQLPlayer(String id, String username, String password, String accessToken) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.accessToken = accessToken;
    }

    
    public String getID() {
        return id;
    }

    
    public String getUsername() {
        return username;
    }

    
    public String getPassword() {
        return password;
    }

    
    public String getAccessToken() {
        return accessToken;
    }

    public static String columnNames() {
        return String.join(",", ID, USERNAME, PASSWORD, ACCESS_TOKEN);
    }
    private String id;
    private String username;
    private String password;
    private String accessToken;
}
