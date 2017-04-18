package edu.goldenhammer.database.postgresql.data_types;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by seanjib on 2/22/2017.
 */
public class SQLMessage {
    public static final String TABLE_NAME = "chat_message";
    public static final String ID = "message_id";
    public static final String GAME_ID = "game_id";
    public static final String PLAYER_ID = "player_id";
    public static final String MESSAGE = "message";
    public static final String CREATE_STMT = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "%2$s SERIAL NOT NULL," +
                    "%3$s INTEGER NOT NULL," +
                    "%4$s INTEGER NOT NULL," +
                    "%5$s VARCHAR(120)," +
                    "PRIMARY KEY(%6$s)," +
                    "FOREIGN KEY(%7$s)" +
                    "   REFERENCES %8$s" +
                    "   ON DELETE CASCADE," +
                    "FOREIGN KEY(%9$s)" +
                    "   REFERENCES %10$s" +
                    "   ON DELETE CASCADE" +
                    ");",
            TABLE_NAME,
            ID,
            GAME_ID,
            PLAYER_ID,
            MESSAGE,
            ID,
            GAME_ID,
            SQLGame.TABLE_NAME,
            PLAYER_ID,
            SQLPlayer.TABLE_NAME
    );


    public SQLMessage(String messageID, String gameID, String playerID, String message) {
        this.messageID = messageID;
        this.gameID = gameID;
        this.playerID = playerID;
        this.message = message;
    }

    
    public String getID() {
        return messageID;
    }

    
    public String getGameID() {
        return gameID;
    }

    
    public String getPlayerID() {
        return playerID;
    }

    
    public String getMessage() {
        return message;
    }

    public static SQLMessage parseResultSetRow(ResultSet resultSet) throws SQLException{
        String id = resultSet.getString(ID);
        String game_id = resultSet.getString(GAME_ID);
        String player_id = resultSet.getString(PLAYER_ID);
        String message = resultSet.getString(MESSAGE);
        return new SQLMessage(id, game_id, player_id, message);
    }

    private String messageID;
    private String gameID;
    private String playerID;
    private String message;
}
