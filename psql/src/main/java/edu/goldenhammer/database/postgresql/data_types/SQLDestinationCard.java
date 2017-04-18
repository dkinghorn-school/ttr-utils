package edu.goldenhammer.database.postgresql.data_types;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class contains the attributes and methods needed to retrieve destination card information from the database
 * and to store it in the database. It has all the SQL code needed to create the table in the database and to insert
 * new destination card values inside.
 */
public class SQLDestinationCard {
    public static final int MAX_DESTINATION_CARDS = 76;
    public static final String TABLE_NAME = "destination_card";
    public static final String ID = "destination_card_id";
    public static final String GAME_ID = "game_id";
    public static final String PLAYER_ID = "player_id";
    public static final String CITY_1 = "city_1";
    public static final String CITY_2 = "city_2";
    public static final String DISCARDED = "discarded";
    public static final String POINTS = "points";
    public static final String DRAWN = "drawn";
    public static final String CREATE_STMT = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (\n" +
                    "%2$s SERIAL NOT NULL,\n" +
                    "%3$s INTEGER NOT NULL,\n" +
                    "%4$s INTEGER,\n" +
                    "%5$s INTEGER NOT NULL,\n" +
                    "%6$s INTEGER NOT NULL,\n" +
                    "%7$s BOOLEAN NOT NULL DEFAULT false,\n" +
                    "%8$s INTEGER NOT NULL," +
                    "%9$s BOOLEAN NOT NULL DEFAULT false," +
                    "PRIMARY KEY(%2$s),\n" +
                    "FOREIGN KEY(%3$s)" +
                    "   REFERENCES %10$s" +
                    "   ON DELETE CASCADE,\n" +
                    "FOREIGN KEY(%4$s)" +
                    "   REFERENCES %11$s" +
                    "   ON DELETE CASCADE,\n" +
                    "FOREIGN KEY (%5$s)" +
                    "   REFERENCES %12$s" +
                    "   ON DELETE CASCADE,\n" +
                    "FOREIGN KEY (%6$s)" +
                    "   REFERENCES %12$s" +
                    "   ON DELETE CASCADE\n" +
                    ");",
            TABLE_NAME,
            ID,
            GAME_ID,
            PLAYER_ID,
            CITY_1,
            CITY_2,
            DISCARDED,
            POINTS,
            DRAWN,
            SQLGame.TABLE_NAME,
            SQLPlayer.TABLE_NAME,
            SQLCity.TABLE_NAME
    );

    /**
     * Constructor for a destination card.
     * @pre no null values, empty strings, or cities that do not exist.
     * @post a new destination card is generated as it is stored in the database.
     * @param destinationCardID The id - from 1 to 76 - of the destination card
     * @param gameID the id of the current game
     * @param city1 the id of the first city listed by the card
     * @param city2 the id of the second city listed by the card
     * @param playerID - the id of the player to whom the card belongs (-1 by default)
     * @param discarded - whether or not the card has been discarded
     * @param points - the point value associated with completion of the destination
     * @param drawn - used during the first phase of drawing destination cards. This indicates
     *              that the card has been drawn, but not yet permanently assigned to the player.
     */
    public SQLDestinationCard(String destinationCardID, String gameID, int city1, int city2,
                              String playerID, boolean discarded, int points, boolean drawn) {
        this.destinationCardID = destinationCardID;
        this.gameID = gameID;
        this.playerID = playerID;
        this.city1 = city1;
        this.city2 = city2;
        this.discarded = discarded;
        this.points = points;
        this.drawn = drawn;
    }

    
    public String getID() {
        return destinationCardID;
    }

    
    public String getGameID() {
        return gameID;
    }

    
    public String getPlayerID() {
        return playerID;
    }

    
    public int getCity1() {
        return city1;
    }

    
    public int getCity2() {
        return city2;
    }

    
    public boolean isDiscarded() {
        return discarded;
    }
    
    
    public int getPoints() {
        return points;
    }

    
    public boolean isDrawn() {
        return drawn;
    }

    /**
     * See getFormattedDestination for formatting directions. This function returns a string containing
     * 76 formatted lines of SQL - one for each card to be inserted in the database.
     *
     * @pre none
     * @post a full string of formatted, ready-to-use SQL code is generated. Each variable must still be
     * assigned after making a prepared statement. See documentation in getFormattedDestination for details.
     * No changes to any classes or variables.
     * @return a string of SQL for a prepared statement
     */
    public static String getAllDestinations() {
        String formattedDestination = "";
        for(int i = 0; i < MAX_DESTINATION_CARDS; i++) {
            formattedDestination += getFormattedDestination();
        }
        return formattedDestination.substring(0, formattedDestination.length() - 2);
    }

    /**
     * Gets a single line of SQL insertion code to enter a single destination card into the database. There are
     * four values that need to be inserted into the PreparedStatement generated from this:
     * -The game name, as a String
     * -The first city name, as a String
     * -The second city name, as a String
     * -The point value of the card, as an int
     * @pre none
     * @post no changes to any classes or variables
     * @return a fully-formatted SQL line
     */
    private static String getFormattedDestination() {
        return String.format("(%1$s, %2$s, %3$s, ?),\n",
                String.format("(SELECT %1$s FROM %2$s WHERE %3$s = ?)",
                        SQLGame.ID,
                        SQLGame.TABLE_NAME,
                        SQLGame.GAME_NAME),
                String.format("(SELECT %1$s FROM %2$s WHERE %3$s = ?)",
                        SQLCity.ID,
                        SQLCity.TABLE_NAME,
                        SQLCity.NAME),
                String.format("(SELECT %1$s FROM %2$s WHERE %3$s = ?)",
                        SQLCity.ID,
                        SQLCity.TABLE_NAME,
                        SQLCity.NAME)
        );
    }

    /**
     *
     * @param resultSet the result of a SELECT query to the table destination_card
     * @return a new SQLDestinationCard that matches the information pulled from the database
     * @pre the ResultSet is not null; it currently points to a row pulled from the destination_card table;
     * the ResultSet includes all columns in the destination_card table
     * @post a new SQLDestinationCard is generated and returned with attributes set to the values from
     * the input ResultSet
     * @throws SQLException
     */
    public static SQLDestinationCard buildDestinationCardFromResultSet(ResultSet resultSet) throws SQLException {
        return new SQLDestinationCard(
                resultSet.getString(ID),
                resultSet.getString(GAME_ID),
                resultSet.getInt(CITY_1),
                resultSet.getInt(CITY_2),
                resultSet.getString(PLAYER_ID),
                resultSet.getBoolean(DISCARDED),
                resultSet.getInt(POINTS),
                resultSet.getBoolean(DRAWN)
        );
    }
    
    private String destinationCardID;
    private String gameID;
    private String playerID;
    private int city1;
    private int city2;
    private boolean discarded;
    private int points;
    private boolean drawn;
}
