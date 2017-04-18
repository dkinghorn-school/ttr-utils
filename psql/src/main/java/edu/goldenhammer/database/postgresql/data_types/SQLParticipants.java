package edu.goldenhammer.database.postgresql.data_types;

import java.io.Serializable;

/**
 * Created by seanjib on 2/3/2017.
 */
public class SQLParticipants implements Serializable {
    public static final String TABLE_NAME = "participants";
    public static final String USER_ID = "user_id";
    public static final String GAME_ID = "game_id";
    public static final String PLAYER_NUMBER = "player_number";
    public static final String POINTS = "points";
    public static final String TRAINS_LEFT = "trains_left";

    public static final String CREATE_STMT = String.format(
                    "CREATE TABLE IF NOT EXISTS %1$s (\n" +
                    "    %2$s INTEGER NOT NULL,\n" +
                    "    %3$s INTEGER NOT NULL,\n" +
                    "    %4$s INTEGER,\n" +
                    "    %5$s INTEGER,\n" +
                    "    %6$s INTEGER,\n" +
                    "    PRIMARY KEY(%2$s, %3$s),\n" +
                    "    FOREIGN KEY(%2$s)\n" +
                    "      REFERENCES %7$s\n" +
                    "      ON DELETE CASCADE,\n" +
                    "    FOREIGN KEY(%3$s)\n" +
                    "      REFERENCES %8$s\n" +
                    "      ON DELETE CASCADE\n" +
                    ");"
            , TABLE_NAME,
            USER_ID,
            GAME_ID,
            PLAYER_NUMBER,
            POINTS,
            TRAINS_LEFT,
            SQLPlayer.TABLE_NAME,
            SQLGame.TABLE_NAME);

    public SQLParticipants(String playerID, String gameID, int playerNumber, int points, int trainsLeft) {
        this.playerID = playerID;
        this.gameID = gameID;
        this.playerNumber = playerNumber;
        this.points = points;
        this.trainsLeft = trainsLeft;
    }

    
    public String getPlayerID() {
        return playerID;
    }

    
    public String getGameID() {
        return gameID;
    }

    
    public int getPlayerNumber() {
        return playerNumber;
    }

    
    public int getPoints() {
        return points;
    }

    
    public int getTrainsLeft() {
        return trainsLeft;
    }

    public static String columnNames() {
        return String.join(",", USER_ID, GAME_ID, PLAYER_NUMBER, POINTS, TRAINS_LEFT);
    }


    private String playerID;
    private String gameID;
    private int playerNumber;
    private int points;
    private int trainsLeft;
}
