package edu.goldenhammer.database.postgresql.data_types;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import edu.goldenhammer.server.commands.BaseCommand;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by seanjib on 2/22/2017.
 */
public class SQLCommand {

    public static final String TABLE_NAME = "command";
    public static final String COMMAND_NUMBER = "command_number";
    public static final String GAME_ID = "game_id";
    public static final String PLAYER_ID = "player_id";
    public static final String COMMAND_TYPE = "command_type";
    public static final String METADATA = "metadata";
    public static final String VISIBLE_TO_SELF = "visible_to_self";
    public static final String VISIBLE_TO_ALL = "visible_to_all";
    public static final String CREATE_STMT = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (" +
                    "%2$s INTEGER NOT NULL," +
                    "%3$s INTEGER NOT NULL," +
                    "%4$s INTEGER NOT NULL," +
                    "%5$s VARCHAR(30) NOT NULL," +
                    "%6$s VARCHAR(1000) NOT NULL," +
                    "%7$s BOOLEAN NOT NULL," +
                    "%8$s BOOLEAN NOT NULL," +
                    "PRIMARY KEY(%2$s, %3$s)," +
                    "FOREIGN KEY(%3$s)" +
                    "   REFERENCES %9$s" +
                    "   ON DELETE CASCADE," +
                    "FOREIGN KEY(%4$s)" +
                    "   REFERENCES %10$s" +
                    "   ON DELETE CASCADE" +
                    ");",
            TABLE_NAME,
            COMMAND_NUMBER,
            GAME_ID,
            PLAYER_ID,
            COMMAND_TYPE,
            METADATA,
            VISIBLE_TO_SELF,
            VISIBLE_TO_ALL,
            SQLGame.TABLE_NAME,
            SQLPlayer.TABLE_NAME
    );

    public SQLCommand(String commandNumber, String gameID, String userID, String commandType, String metadata, boolean visibleToSelf, boolean visibleToAll) {
        this.commandNumber = commandNumber;
        this.gameID = gameID;
        this.userID = userID;
        this.commandType = commandType;
        this.metadata = metadata;
        this.visibleToSelf = visibleToSelf;
        this.visibleToAll = visibleToAll;
    }

    public String getCommandNumber() {
        return commandNumber;
    }

    
    public String getGameID() {
        return gameID;
    }

    
    public String getUserID() {
        return userID;
    }

    
    public String getCommandType() {
        return commandType;
    }

    
    public String getMetadata() {
        return metadata;
    }

    
    public boolean isVisibleToSelf() {
        return visibleToSelf;
    }

    
    public boolean isVisibleToAll() {
        return visibleToAll;
    }

    public static BaseCommand buildCommandFromResultSet(ResultSet resultSet, String player_name) throws SQLException{
        String commandName = resultSet.getString(COMMAND_TYPE);
        StringBuilder sb = new StringBuilder(commandName);
        commandName = sb.replace(0, 1, sb.substring(0, 1).toUpperCase()).toString();
        String packagePrefix = "edu.goldenhammer.server.commands.";
        String className = packagePrefix + commandName + "Command";

        BaseCommand command = null;
        try {
            Class c = null;
            try {
                c = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }

            String metadata = resultSet.getString(METADATA);

            Gson gson = new Gson();
            command = (BaseCommand)gson.fromJson(metadata, c);
            command.setPlayerNumber(resultSet.getInt("player_number"));
            if(!command.getPlayerName().equals(player_name) && !resultSet.getBoolean(VISIBLE_TO_ALL)){
                command.hide();
            }
        } catch (JsonSyntaxException ex) {
            ex.printStackTrace();
        }
        return command;
    }

    private String commandNumber;
    private String gameID;
    private String userID;
    private String commandType;
    private String metadata;
    private boolean visibleToSelf;
    private boolean visibleToAll;
}
