package edu.goldenhammer.database.postgresql;

import com.google.gson.Gson;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.database.Lock;
import edu.goldenhammer.database.postgresql.data_types.*;
import edu.goldenhammer.model.*;
import edu.goldenhammer.server.Serializer;
import edu.goldenhammer.server.commands.BaseCommand;
import edu.goldenhammer.server.commands.EndTurnCommand;
import edu.goldenhammer.server.commands.InitializeHandCommand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by seanjib on 4/9/2017.
 */
public class SQLController implements IDatabaseController {
    int MAX_TRAIN;
    private SQLConnectionFactory session;

    public SQLController(){
        MAX_TRAIN=45;
        initializeDatabase();
    }

    public SQLController(int maxTrain){
        MAX_TRAIN=maxTrain;
        initializeDatabase();
    }

    private void initializeDatabase() {
        this.session = SQLConnectionFactory.getInstance();
        ensureTablesCreated();
    }

    private void ensureTablesCreated() {
        createTable(SQLGame.CREATE_STMT);
        createTable(SQLPlayer.CREATE_STMT);
        createTable(SQLParticipants.CREATE_STMT);
        createTable(SQLCity.CREATE_STMT);
        createTable(SQLRoute.CREATE_STMT);
        createTable(SQLClaimedRoute.CREATE_STMT);
        createTable(SQLDestinationCard.CREATE_STMT);
        createTable(SQLTrainCard.CREATE_STMT);
        createTable(SQLCommand.CREATE_STMT);
        createTable(SQLMessage.CREATE_STMT);
        createTable(SQLInitialGameModel.CREATE_STMT);
        initializeCities();
        initializeRoutes();
    }

    private void createTable(String sqlStatementString) {
        try (Connection connection = session.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sqlStatementString);
            statement.execute();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * @param player_user_name the username associated with the Player to be returned
     * @pre the database connection is valid and the player_user_name is associated with a user already stored and the database schema has not been altered
     * @post The Player is read from the database
     * @return The Player associated with the username is returned
     */
    @Override
    public Player getPlayerInfo(String player_user_name) {
        try (Connection connection = session.getConnection()){
            String sqlString = String.format("SELECT %1$s FROM %2$s WHERE %3$s = ?",
                    SQLPlayer.columnNames(),
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, player_user_name);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                return new Player(
                        resultSet.getString(SQLPlayer.USERNAME),
                        resultSet.getString(SQLPlayer.ACCESS_TOKEN)
                );
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    private GameList getGameListFromResultSet (ResultSet resultSet) throws SQLException{
        GameList gameList = new GameList();
        GameListItem game = null;
        while(resultSet.next()){
            String user_id = resultSet.getString((SQLPlayer.USERNAME));
            String game_id = resultSet.getString(SQLParticipants.GAME_ID);
            boolean started = resultSet.getBoolean(SQLGame.STARTED);
            if(game == null || !game_id.equals(game.getID())){

                String name = resultSet.getString(SQLGame.GAME_NAME);
                game = new GameListItem(game_id, name, started, new ArrayList<>());
                gameList.add(game);
            }
            game.getPlayers().add(user_id);
        }
        return gameList;
    }

    /**
     * @pre the connection to the SQL database is valid and the database schema has not been altered
     * @post all games that are not started are returned
     * @return all games that have not been started
     */
    @Override
    public GameList getGames() {

        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT %1$s, %2$s, %3$s, %7$s FROM %4$s NATURAL JOIN %5$s " +
                            "NATURAL JOIN %6$s WHERE %7$s = false ORDER BY %8$s",
                    SQLParticipants.columnNames(),
                    SQLGame.GAME_NAME,
                    SQLPlayer.USERNAME,
                    SQLParticipants.TABLE_NAME,
                    SQLGame.TABLE_NAME,
                    SQLPlayer.TABLE_NAME,
                    SQLGame.STARTED,
                    SQLGame.ID);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            ResultSet resultSet = statement.executeQuery();
            return getGameListFromResultSet(resultSet);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }
    /**
     * @param player_user_name the name of the user to find all games associated with
     * @pre the connection to the SQL database is valid
     * @post all games in which the player_user_name are involved is returned
     * @return all games in which the player is a participant
     */
    @Override
    public GameList getGames(String player_user_name) {

        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT %1$s, %2$s, %3$s, %15$s FROM %4$s NATURAL JOIN %5$s\n" +
                            "NATURAL JOIN %6$s WHERE %7$s IN (SELECT %8$s FROM %9$s\n" +
                            "WHERE %10$s IN (SELECT %11$s FROM %12$s WHERE %13$s=?)) ORDER BY %14$s",
                    SQLParticipants.columnNames(),
                    SQLGame.GAME_NAME,
                    SQLPlayer.USERNAME,
                    SQLParticipants.TABLE_NAME,

                    SQLGame.TABLE_NAME,
                    SQLPlayer.TABLE_NAME,
                    SQLGame.ID,
                    SQLParticipants.GAME_ID,

                    SQLParticipants.TABLE_NAME,
                    SQLParticipants.USER_ID,
                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,

                    SQLPlayer.USERNAME,
                    SQLGame.ID,
                    SQLGame.STARTED);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,player_user_name);
            ResultSet resultSet = statement.executeQuery();
            return getGameListFromResultSet(resultSet);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param username users username for the person logging in
     * @param password the password for the person logging in
     * @pre the database schema has not been altered
     * @post the method will return true if a record with the same username and password exist in the database
     * @return returns true if the player exists else false
     */
    @Override
    public Boolean login(String username, String password) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT %1$s FROM %2$s WHERE %3$s = ? AND %4$s = ?",
                    SQLPlayer.columnNames(),
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,
                    SQLPlayer.PASSWORD);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,username);
            statement.setString(2,password);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next(); //if there is one result for the username and password input, it is a valid login
        } catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param username the username wanting to be stored for a new user
     * @param password the password to be used for logging in later.
     * @pre the database schema has not been altered, the username is not in use
     * @post the associated username and password will be added to the database.
     * @return if the new user was created true, else false is returned
     */
    @Override
    public Boolean createUser(String username, String password) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("INSERT INTO %1$s(%2$s,%3$s) VALUES (?,?)",
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,
                    SQLPlayer.PASSWORD);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,username);
            statement.setString(2,password);
            return  statement.executeUpdate() > 0;
        } catch(SQLException e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param name the name the new game should have.
     * @pre the database schema has not been altered, and another game with the same name doesn't exist
     * @post a new game will be created with the name.
     * @return if a game with that name was created
     */
    @Override
    public Boolean createGame(String name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("INSERT INTO %1$s(%2$s,%3$s) VALUES (?,?)",
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,
                    SQLGame.STARTED);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,name);
            statement.setBoolean(2,false);
            return  statement.executeUpdate() > 0;
        } catch(SQLException e){
        }
        return false;
    }

    /**
     *
     * @param player_name username of the player to be added to the game
     * @param game_name the name of the game the player above should be added to.
     * @pre the database schema has not been altered, The game exists with fewer than 5 participants and has not yet started, and the player exists
     * @post the player will be added to the game.
     * @return returns true if the player was added to the game else it returns false.
     */
    @Override
    public Boolean joinGame(String player_name, String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT count(*) FROM (\n" +
                            "SELECT * FROM %1$s INNER JOIN (\n" +
                            "SELECT %2$s FROM %3$s WHERE %4$s = ? AND %5$s = false\n" +
                            ") AS game_ids ON (%1$s.%6$s = game_ids.%2$s)\n" +
                            ") AS participant_count;",
                    SQLParticipants.TABLE_NAME,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,
                    SQLGame.STARTED,

                    SQLParticipants.GAME_ID);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,game_name);

            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                int currentParticipantsCount = resultSet.getInt(1);
                if(currentParticipantsCount < 5) {
                    sqlString = String.format("INSERT INTO %1$s(%2$s, %3$s)" +
                                    "VALUES (" +
                                    "(SELECT %4$s FROM %5$s WHERE %6$s = ?)," +
                                    "(SELECT %7$s FROM %8$s WHERE %9$s =  ?)" +
                                    ");",
                            SQLParticipants.TABLE_NAME,
                            SQLParticipants.USER_ID,
                            SQLParticipants.GAME_ID,

                            SQLPlayer.ID,
                            SQLPlayer.TABLE_NAME,
                            SQLPlayer.USERNAME,

                            SQLGame.ID,
                            SQLGame.TABLE_NAME,
                            SQLGame.GAME_NAME);
                    statement = connection.prepareStatement(sqlString);
                    statement.setString(1, player_name);
                    statement.setString(2, game_name);

                    return (statement.executeUpdate() != 0);
                }
            }

        } catch (SQLException e) {
        }
        return false;
    }


    private List<String> getPlayerNamesFromResultSet (ResultSet resultSet) throws SQLException{
        List<String> playerList = new ArrayList<>();
        while(resultSet.next()){
            String username = resultSet.getString((SQLPlayer.USERNAME));
            playerList.add(username);
        }
        return playerList;
    }

    /**
     *
     * @param game_name the name of the game to find players associated with
     * @pre the database schema has not been altered
     * @post all players involved in a game will be returned.
     * @return the list of usernames of players that are a member of the game
     */
    @Override
    public List<String> getPlayers(String game_name) {
        try (Connection connection = session.getConnection()) {

            //get the information to make the GameModel object from the database
            String sqlString = String.format("SELECT %1$s,%2$s FROM %3$s NATURAL JOIN %4$s\n" +
                            "WHERE %5$s IN (SELECT %6$s FROM %7$s WHERE %8$s = ?)",
                    SQLPlayer.ID,
                    SQLPlayer.USERNAME,
                    SQLPlayer.TABLE_NAME,
                    SQLParticipants.TABLE_NAME,
                    SQLParticipants.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,game_name);
            ResultSet resultSet = statement.executeQuery();

            return getPlayerNamesFromResultSet(resultSet);
        } catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param player_name the player username to leave the game
     * @param game_name the name of the game to remove the player from
     * @pre the player is part of the game and the game has not yet started
     * @post the player will be removed from the game
     * @return returns true if the player left the game else false
     */
    @Override
    public Boolean leaveGame(String player_name, String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("DELETE FROM %1$s WHERE " +
                            "%2$s IN (SELECT %3$s FROM %4$s WHERE %5$s = ?)" +
                            "AND %6$s IN (SELECT %7$s FROM %8$s WHERE %9$s = ? AND started = false)",
                    SQLParticipants.TABLE_NAME,
                    SQLParticipants.USER_ID,
                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,

                    SQLParticipants.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,player_name);
            statement.setString(2,game_name);
            return (statement.executeUpdate() != 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     *
     * @param game_name the name of the game to start
     * @pre the game has not yet started
     * @post the game will begin and no one will be able to join the game anymore
     * @return the IGameModel associated with the game
     */
    @Override
    public IGameModel playGame(String game_name) {
        synchronized(Lock.getInstance().getLock(game_name)) {
            try (Connection connection = session.getConnection()) {
                //get the information to make the GameModel object from the database
                String sqlString = String.format("SELECT %1$s FROM %2$s WHERE %3$s = ?",
                        SQLGame.STARTED,
                        SQLGame.TABLE_NAME,
                        SQLGame.GAME_NAME);
                PreparedStatement statement = connection.prepareStatement(sqlString);
                statement.setString(1, game_name);
                ResultSet resultSet = statement.executeQuery();

                List<String> players = getPlayers(game_name);
                if (resultSet.next() && players.size() > 1) {
                    if (!resultSet.getBoolean(SQLGame.STARTED)) {
                        initializeGame(game_name);
                        GameModel gameModel = getGameModel(game_name);
                        for(PlayerOverview player: gameModel.getPlayers()) {
                            player.setDestCards(0);
                        }
                        setInitialGameModel(gameModel, game_name);
                    }
                    return getInitialGameModel(game_name);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public GameModel getGameModel(String game_name) {
        List<PlayerOverview> players = getPlayerOverviews(game_name);
        List<DestinationCard> destinationDeck = getDestinationCards(game_name);
        List<TrainCard> trainCardDeck = getTrainCards(game_name);
        Map map = getMap(game_name);
        GameName gameName = new GameName(game_name);
        return new GameModel(players, map, gameName, getSlotCardColors(game_name));
    }

    @Override
    public void updateCurrentPlayer(String game_name, int nextPlayer) {

    }

    private void setInitialGameModel(IGameModel initialModel, String gameName) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format(
                    "insert into %1$s (%2$s, %3$s)values(?, ?)"
                    , SQLInitialGameModel.TABLE_NAME
                    , SQLInitialGameModel.GAME_NAME
                    , SQLInitialGameModel.INITIAL_STATE);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, gameName);
            statement.setString(2, new Gson().toJson(initialModel));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GameModel getInitialGameModel(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format(
                    "select * from %1$s where %2$s=?"
                    , SQLInitialGameModel.TABLE_NAME,
                    SQLInitialGameModel.GAME_NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            return SQLInitialGameModel.buildFromResultsSet(statement.executeQuery());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<PlayerOverview> getPlayerOverviews(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT %5$s.%1$s, %5$s.%2$s, %5$s.%3$s, %5$s.%4$s, usernames.%8$s\n" +
                            "FROM %5$s INNER JOIN (SELECT %7$s, %8$s FROM %9$s) AS usernames\n" +
                            "ON %5$s.%6$s = usernames.%7$s\n" +
                            "WHERE %5$s.%10$s IN (SELECT %11$s FROM %12$s WHERE %13$s = ?)",
                    SQLParticipants.USER_ID,
                    SQLParticipants.PLAYER_NUMBER,
                    SQLParticipants.POINTS,
                    SQLParticipants.TRAINS_LEFT,

                    SQLParticipants.TABLE_NAME,
                    SQLParticipants.USER_ID,
                    SQLPlayer.ID,
                    SQLPlayer.USERNAME,

                    SQLPlayer.TABLE_NAME,
                    SQLParticipants.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,

                    SQLGame.GAME_NAME
            );
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            ResultSet resultSet = statement.executeQuery();

            List<PlayerOverview> players = new ArrayList<>();
            while(resultSet.next()) {
                Color color = Color.getPlayerColorFromNumber(resultSet.getInt(SQLParticipants.PLAYER_NUMBER));
                int pieces = resultSet.getInt(SQLParticipants.TRAINS_LEFT);
                int destCards = getDestinationCardCount(resultSet.getInt(SQLParticipants.USER_ID));
                int player = resultSet.getInt(SQLParticipants.PLAYER_NUMBER);
                String username = resultSet.getString(SQLPlayer.USERNAME);
                int points = resultSet.getInt(SQLParticipants.POINTS);

                players.add(new PlayerOverview(color, pieces, destCards, player, username, points));
            }
            return players;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private int getDestinationCardCount(String player_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT count(*) FROM %1$s WHERE %2$s = (SELECT %3$s FROM %4$s WHERE %5$s = ?)",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.PLAYER_ID,

                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, player_name);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private int getDestinationCardCount(int player_id) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT count(*) FROM %1$s WHERE %2$s = ?",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.PLAYER_ID);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, player_id);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    private List<DestinationCard> getDestinationCards(String game_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT %1$s.%2$s,\n" +
                            "city1.%9$s AS city1_point_x," +
                            "city1.%10$s AS city1_point_y,\n" +
                            "city1.%11$s AS city1_name,\n" +
                            "%1$s.%3$s,\n" +
                            "city2.%9$s AS city2_point_x,\n" +
                            "city2.%10$s AS city2_point_y,\n" +
                            "city2.%11$s AS city2_name, \n" +
                            "%1$s.%4$s \n" +
                            "FROM %1$s\n" +
                            "INNER JOIN %7$s AS city1 ON %1$s.%2$s = city1.%8$s \n" +
                            "INNER JOIN %7$s AS city2 ON %1$s.%3$s = city2.%8$s \n" +
                            "WHERE %5$s = ? AND %6$s = ?",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.CITY_1,
                    SQLDestinationCard.CITY_2,
                    SQLDestinationCard.POINTS,
                    SQLDestinationCard.DISCARDED,
                    SQLDestinationCard.DRAWN,

                    SQLCity.TABLE_NAME,
                    SQLCity.ID,
                    SQLCity.POINT_X,
                    SQLCity.POINT_Y,
                    SQLCity.NAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setBoolean(1, false);
            statement.setBoolean(2, false);
            ResultSet resultSet = statement.executeQuery();

            List<DestinationCard> destinationCards = new ArrayList<>();
            while(resultSet.next()) {
                String name_1 = resultSet.getString("city1_name");
                int x_location_1 = resultSet.getInt("city1_point_x");
                int y_location_1 = resultSet.getInt("city1_point_y");

                String name_2 = resultSet.getString("city2_name");
                int x_location_2 = resultSet.getInt("city2_point_x");
                int y_location_2 = resultSet.getInt("city2_point_y");

                City city1 = new City(x_location_1, y_location_1, name_1);
                City city2 = new City(x_location_2, y_location_2, name_2);

                int pointsWorth = resultSet.getInt(SQLDestinationCard.POINTS);
                destinationCards.add(new DestinationCard(city1, city2, pointsWorth));
            }
            return destinationCards;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private List<TrainCard> getTrainCards(String game_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT %1$s FROM %2$s \n" +
                            "WHERE %3$s IS NULL\n" +
                            "AND %4$s = false\n" +
                            "AND %5$s = (SELECT %6$s FROM %7$s WHERE %8$s = ?);",
                    SQLTrainCard.TRAIN_TYPE,
                    SQLTrainCard.TABLE_NAME,
                    SQLTrainCard.PLAYER_ID,
                    SQLTrainCard.DISCARDED,
                    SQLTrainCard.GAME_ID,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);

            ResultSet resultSet = statement.executeQuery();

            List<TrainCard> trainCards = new ArrayList<>();
            while(resultSet.next()) {
                Color color = Color.getTrainCardColorFromString(resultSet.getString(SQLTrainCard.TRAIN_TYPE));
                trainCards.add(new TrainCard(color));
            }
            return trainCards;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private Map getMap(String game_name) {
        List<Track> tracks = getTracks(game_name);
        List<City> cities = getCities();

        return new Map(tracks, cities);
    }

    @Override
    public List<Track> getTracks(String game_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format(
                    "with tracks as (SELECT %1$s.%3$s,\n" +
                            "city1.%8$s AS city1_point_x, city1.%9$s AS city1_point_y, city1.%10$s AS city1_name,\n" +
                            "%1$s.%4$s,\n" +
                            "city2.%8$s AS city2_point_x, city2.%9$s AS city2_point_y, city2.%10$s AS city2_name,\n" +
                            "%1$s.%5$s, %1$s.%22$s, route_player_numbers.%18$s, route.route_number FROM %1$s\n" +
                            "INNER JOIN %6$s AS city1 ON %1$s.%3$s = city1.%7$s \n" +
                            "INNER JOIN %6$s AS city2 ON %1$s.%4$s = city2.%7$s\n" +
                            "LEFT JOIN (SELECT %11$s.%12$s, %11$s.%13$s,\n" +
                            "\t%11$s.%14$s, player_numbers.%18$s FROM %11$s\n" +
                            "\tINNER JOIN (SELECT %15$s.%16$s, %15$s.%17$s, %15$s.%18$s FROM %15$s\n" +
                            "\t\tWHERE %16$s = (SELECT %20$s FROM %19$s WHERE %21$s = ?))\n" +
                            "\t AS player_numbers\n" +
                            "\t ON %11$s.%12$s = player_numbers.%16$s\n" +
                            "\t AND %11$s.%13$s = player_numbers.%17$s)\n" +
                            "AS route_player_numbers\n" +
                            "ON route_player_numbers.%14$s = %1$s.%2$s)" +
                            "select *, (select (count(*)=1) as second from tracks t2 where t1.route_number > t2.route_number and t1.city_1=t2.city_1 and t1.city_2=t2.city_2)\n" +
                            "from tracks t1;",
                    SQLRoute.TABLE_NAME, //1
                    SQLRoute.ROUTE_NUMBER,
                    SQLRoute.CITY_1,
                    SQLRoute.CITY_2,
                    SQLRoute.ROUTE_LENGTH,

                    SQLCity.TABLE_NAME, //6
                    SQLCity.ID,
                    SQLCity.POINT_X,
                    SQLCity.POINT_Y,
                    SQLCity.NAME,

                    SQLClaimedRoute.TABLE_NAME, //11
                    SQLClaimedRoute.GAME_ID,
                    SQLClaimedRoute.PLAYER_ID,
                    SQLClaimedRoute.ROUTE_ID,

                    SQLParticipants.TABLE_NAME, //15
                    SQLParticipants.GAME_ID,
                    SQLParticipants.USER_ID,
                    SQLParticipants.PLAYER_NUMBER,

                    SQLGame.TABLE_NAME, //19
                    SQLGame.ID,
                    SQLGame.GAME_NAME,

                    SQLRoute.ROUTE_COLOR);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);

            ResultSet resultSet = statement.executeQuery();

            List<Track> tracks = new ArrayList<>();
            while(resultSet.next()) {
                int location1x = resultSet.getInt("city1_point_x");
                int location1y = resultSet.getInt("city1_point_y");
                String cityName1 = resultSet.getString("city1_name");
                int location2x = resultSet.getInt("city2_point_x");
                int location2y = resultSet.getInt("city2_point_y");
                String cityName2 = resultSet.getString("city2_name");

                City city1 = new City(location1x, location1y, cityName1);
                City city2 = new City(location2x, location2y, cityName2);
                int route_number = resultSet.getInt("route_number");
                int length = resultSet.getInt(SQLRoute.ROUTE_LENGTH);
                boolean second = resultSet.getBoolean("second");
                Color color = Color.getTrackColorFromString(resultSet.getString(SQLRoute.ROUTE_COLOR));
                int owner = resultSet.getInt(SQLParticipants.PLAYER_NUMBER);
                if(resultSet.wasNull()) {
                    owner = -1;
                }

                tracks.add(new Track(city1, city2, length, color, owner, location1x, location1y, location2x, location2y, route_number, second));
            }
            return tracks;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private List<City> getCities() {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT * FROM %1$s",
                    SQLCity.TABLE_NAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            ResultSet resultSet = statement.executeQuery();

            List<City> cities = new ArrayList<>();
            while(resultSet.next()) {
                int x_location = resultSet.getInt(SQLCity.POINT_X);
                int y_location = resultSet.getInt(SQLCity.POINT_Y);
                String name = resultSet.getString(SQLCity.NAME);

                cities.add(new City(x_location, y_location, name));
            }
            return cities;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     *
     * @param player_user_name the username of the player to set the access token
     * @param accessToken the new access token to be associated with the player
     * @pre the username exists
     * @post the database will store the accessToken for the associated user
     */
    @Override
    public void setAccessToken(String player_user_name, String accessToken){
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("UPDATE %1$s SET %2$s = ? WHERE %3$s = ?",
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.ACCESS_TOKEN,
                    SQLPlayer.USERNAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,accessToken);
            statement.setString(2,player_user_name);
            statement.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    /**
     *
     * @param gameName name of the game to possibly drop
     * @pre no one is participating in the game
     * @post the game will be dropped
     */
    @Override
    public void maybeDropGame(String gameName) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("DELETE FROM %1$s WHERE %2$s=?\n" +
                            "AND %3$s NOT IN (SELECT %4$s FROM %5$s)",
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,
                    SQLGame.ID,
                    SQLParticipants.GAME_ID,
                    SQLParticipants.TABLE_NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1,gameName);
            statement.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    private void initializeGame(String game_name) {
        initializeParticipants(game_name);
        initializeTrainCards(game_name);
        initializeDestinationCards(game_name);
        initializeHands(game_name);
//        initializeTurn(game_name);
        setGameStarted(game_name);
    }

    private void initializeTurn(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("WITH last_command AS\n" +
                    "   (SELECT command_number, player_id, game_id FROM command\n" +
                    "   WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "   ORDER BY command_number DESC\n" +
                    "   LIMIT 1),\n" +
                    "last_command_player_number AS \n" +
                    "   (SELECT player_number FROM participants\n" +
                    "   WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "   AND user_id IN (SELECT player_id FROM last_command)),\n" +
                    "current_player_name AS\n" +
                    "   (SELECT username FROM player WHERE user_id IN\n" +
                    "       (SELECT user_id FROM participants\n" +
                    "       WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "       AND player_number IN (SELECT player_number FROM last_command_player_number)))\n" +
                    "SELECT * FROM last_command, last_command_player_number, current_player_name;\n");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setString(2, game_name);
            statement.setString(3, game_name);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                int player_number = resultSet.getInt(SQLParticipants.PLAYER_NUMBER);
                int command_number = resultSet.getInt(SQLCommand.COMMAND_NUMBER) + 1;
                String player_name = resultSet.getString(SQLPlayer.USERNAME);
                int previousPlayer = resultSet.getInt(SQLParticipants.PLAYER_NUMBER) - 1;
                int nextPlayer = 0;

                EndTurnCommand endTurn = new EndTurnCommand();
                endTurn.setPlayerNumber(player_number);
                endTurn.setCommandNumber(command_number);
                endTurn.setGameName(game_name);
                endTurn.setPlayerName(player_name);
                endTurn.setPreviousPlayer(previousPlayer);
                endTurn.setNextPlayer(nextPlayer);


                endTurn.execute();
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void setGameStarted(String game_name) {
        try (Connection connection = session.getConnection()){
            String sqlString = String.format("UPDATE %1$s SET %2$s = ? WHERE %3$s = ?",
                    SQLGame.TABLE_NAME,
                    SQLGame.STARTED,
                    SQLGame.GAME_NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setBoolean(1, true);
            statement.setString(2, game_name);
            statement.executeUpdate();

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeHands(String game_name) {
        List<String> players = getPlayers(game_name);
        int commandNumber = 0;
        for(String player : players) {
            BaseCommand command = new InitializeHandCommand();
            command.setGameName(game_name);
            command.setCommandNumber(commandNumber);
            commandNumber++;
            command.setPlayerName(player);
            command.execute();
        }
    }

    private void initializeParticipants(String game_name) {
        List<String> players = getPlayers(game_name);
        try (Connection connection = session.getConnection()){
            for (int i = 0; i < players.size(); i++) {
                String sqlString = String.format("UPDATE %1$s SET %2$s = ?, %3$s = 0, %4$s = ?\n" +
                                " WHERE %5$s IN (SELECT %6$s FROM %7$s WHERE %8$s = ?)\n" +
                                " AND %9$s IN (SELECT %10$s FROM %11$s WHERE %12$s = ?);",
                        SQLParticipants.TABLE_NAME,
                        SQLParticipants.PLAYER_NUMBER,
                        SQLParticipants.POINTS,
                        SQLParticipants.TRAINS_LEFT,

                        SQLParticipants.GAME_ID,
                        SQLGame.ID,
                        SQLGame.TABLE_NAME,
                        SQLGame.GAME_NAME,

                        SQLParticipants.USER_ID,
                        SQLPlayer.ID,
                        SQLPlayer.TABLE_NAME,
                        SQLPlayer.USERNAME
                );

                PreparedStatement statement = connection.prepareStatement(sqlString);
                statement.setInt(1, i);
                statement.setInt(2, MAX_TRAIN);
                statement.setString(3, game_name);
                statement.setString(4, players.get(i));
                statement.executeUpdate();

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initializeTrainCards(String game_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("INSERT INTO %1$s(%2$s, %3$s, %4$s) VALUES %5$s",
                    SQLTrainCard.TABLE_NAME,
                    SQLTrainCard.ID,
                    SQLTrainCard.GAME_ID,
                    SQLTrainCard.TRAIN_TYPE,
                    SQLTrainCard.getAllTrainCards());

            PreparedStatement statement = connection.prepareStatement(sqlString);
            for(int i = 0; i < ((SQLTrainCard.MAX_COLORED_CARDS * 8) + SQLTrainCard.MAX_WILD_CARDS); i++) {
                statement.setInt(i * 2 + 1, i);
                statement.setString(i * 2 + 2, game_name);
            }
            statement.execute();
            initializeSlots(game_name);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initializeSlots(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("WITH random_cards AS (\n" +
                    "SELECT train_card_id FROM train_card\n" +
                    "WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "AND discarded = false\n" +
                    "AND player_id IS NULL\n" +
                    "ORDER BY random()\n" +
                    "LIMIT 5),\n" +
                    "cards_and_slots AS (SELECT row_number() over() as slot, * FROM random_cards)\n" +
                    "UPDATE train_card SET slot = cards_and_slots.slot - 1\n" +
                    "FROM cards_and_slots\n" +
                    "WHERE game_id IN (SELECT game_id FROM game WHERE name=?)\n" +
                    "AND train_card.train_card_id = cards_and_slots.train_card_id;\n");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setString(2, game_name);
            statement.execute();
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void initializeDestinationCards(String game_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("INSERT INTO %1$s(%2$s, %3$s, %4$s, %5$s) VALUES\n%6$s",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.GAME_ID,
                    SQLDestinationCard.CITY_1,
                    SQLDestinationCard.CITY_2,
                    SQLDestinationCard.POINTS,
                    SQLDestinationCard.getAllDestinations());

            PreparedStatement statement = connection.prepareStatement(sqlString);
            String pathName = "/destinations.txt";
            Scanner destinations = new Scanner(getClass().getResourceAsStream(pathName));
            for(int i = 0; i < SQLDestinationCard.MAX_DESTINATION_CARDS * 4; i += 4) {
                String destination = destinations.nextLine();
                String[] vars = destination.split(",");
                statement.setString(i + 1, game_name);
                statement.setString(i + 2, vars[0]);
                statement.setString(i + 3, vars[1]);
                statement.setInt(i + 4, Integer.parseInt(vars[2]));
            }
            statement.execute();

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initializeCities() {
        try(Connection connection = session.getConnection()) {
            String sqlString = SQLCity.INSERT_STMT;
            PreparedStatement statement = connection.prepareStatement(sqlString);
            String pathName = "/cities.txt";
            Scanner cities = new Scanner(getClass().getResourceAsStream(pathName));
            for (int i = 0; i < SQLCity.CITY_COUNT * 3; i += 3) {
                String city = cities.nextLine();
                String[] vars = city.split(",");

                statement.setString(i + 1, vars[0]); //setting city name
                statement.setInt(i + 2, Integer.parseInt(vars[1])); //point x
                statement.setInt(i + 3, Integer.parseInt(vars[2])); //point y
            }
            statement.execute();
        } catch (SQLException ex) {
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initializeRoutes() {
        try(Connection connection = session.getConnection()) {
            String sqlString = SQLRoute.INSERT_STMT;
            PreparedStatement statement = connection.prepareStatement(sqlString);

            String pathName = "/routes.txt";
            Scanner routes = new Scanner(getClass().getResourceAsStream(pathName));
            for (int i = 0; i < SQLRoute.ROUTE_COUNT * 5; i += 5) {
                String route = routes.nextLine();
                String[] vars = route.split(",");

                statement.setInt(i + 1, (i / 5) + 1); //routeID
                statement.setString(i + 2, vars[0]); //city1
                statement.setString(i + 3, vars[1]); //city2
                statement.setString(i + 4, vars[2]); //color
                statement.setInt(i + 5, Integer.parseInt(vars[3])); //points
            }
            statement.execute();
        } catch (SQLException ex) {
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public TrainCard drawRandomTrainCard(String game_name, String player_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("UPDATE %1$s SET %6$s = (SELECT %8$s FROM %9$s WHERE %10$s = ?)\n" +
                            "WHERE %11$s IN (\n" +
                            "               SELECT %11$s FROM %1$s\n" +
                            "              WHERE %2$s IN (SELECT %3$s FROM %4$s WHERE %5$s = ?)\n" +
                            "              AND %6$s IS NULL\n" +
                            "              AND %12$s IS NULL\n" +
                            "              AND %7$s = false\n" +
                            "              ORDER BY random() LIMIT 1)\n " +
                            "and %2$s in (SELECT %3$s FROM %4$s WHERE %5$s = ?)\n " +
                            "RETURNING *;",
                    SQLTrainCard.TABLE_NAME,
                    SQLTrainCard.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,

                    SQLGame.GAME_NAME,
                    SQLTrainCard.PLAYER_ID,
                    SQLTrainCard.DISCARDED,
                    SQLPlayer.ID,

                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,
                    SQLTrainCard.ID,
                    SQLTrainCard.SLOT);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, player_name);
            statement.setString(2, game_name);
            statement.setString(3, game_name);
            ResultSet resultSet = statement.executeQuery();

            SQLTrainCard card;
            if(resultSet.next()){
                card = SQLTrainCard.buildTrainCardFromResultSet(resultSet);
                return new TrainCard(Color.getTrainCardColorFromString(card.getTrainType()));
            }
            else if(reshuffleTrainCardDiscardPile(game_name)) {
                resultSet = statement.executeQuery();
                card = SQLTrainCard.buildTrainCardFromResultSet(resultSet);
                return new TrainCard(Color.getTrainCardColorFromString(card.getTrainType()));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public TrainCard drawTrainCardFromSlot(String game_name, String player_name, int slot) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format(
                    "WITH selected_card AS\n" +
                            "\t(SELECT train_card_id FROM train_card\n" +
                            "\tWHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                            "\tAND slot = ?),\n" +
                            "random_card AS\n" +
                            "    (SELECT train_card_id FROM train_card\n" +
                            "     WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                            "     AND slot IS NULL\n" +
                            "     AND player_id IS NULL\n" +
                            "     AND discarded = false\n" +
                            "     ORDER BY random()\n" +
                            "     LIMIT 1),\n" +
                            "new_slot_card AS \n" +
                            "\t(UPDATE train_card SET slot = ?\n" +
                            "     FROM random_card\n" +
                            "     WHERE train_card.train_card_id = random_card.train_card_id\n" +
                            "     AND game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                            "     RETURNING *)\n" +
                            "UPDATE train_card SET player_id = (SELECT user_id FROM player WHERE username = ?),\n" +
                            "slot = NULL\n" +
                            "FROM selected_card, new_slot_card\n" +
                            "WHERE train_card.train_card_id = selected_card.train_card_id\n" +
                            "AND train_card.game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                            "AND EXISTS (SELECT count(*) FROM new_slot_card)\n" +
                            "RETURNING *");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setInt(2, slot);
            statement.setString(3, game_name);
            statement.setInt(4, slot);
            statement.setString(5, game_name);
            statement.setString(6, player_name);
            statement.setString(7, game_name);
            ResultSet resultSet = statement.executeQuery();

            SQLTrainCard card;
            if(resultSet.next()) {
                card = SQLTrainCard.buildTrainCardFromResultSet(resultSet);
                return new TrainCard(Color.getTrainCardColorFromString(card.getTrainType()));
            }
            else if(reshuffleTrainCardDiscardPile(game_name)) {
                resultSet = statement.executeQuery();
                resultSet.next();
                card = SQLTrainCard.buildTrainCardFromResultSet(resultSet);
                return new TrainCard(Color.getTrainCardColorFromString(card.getTrainType()));
            }
            else {
                return drawTrainCardFromSlotWithoutReplacement(game_name, player_name, slot);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private TrainCard drawTrainCardFromSlotWithoutReplacement(String game_name, String player_name, int slot) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format(
                    "WITH selected_card AS\n" +
                            "\t(SELECT train_card_id FROM train_card\n" +
                            "\tWHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                            "\tAND slot = ?)\n" +
                            "UPDATE train_card SET player_id = (SELECT user_id FROM player WHERE username = ?),\n" +
                            "slot = NULL\n" +
                            "FROM selected_card\n" +
                            "WHERE train_card.train_card_id = selected_card.train_card_id\n" +
                            "AND train_card.game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                            "RETURNING *");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setInt(2, slot);
            statement.setString(3, player_name);
            statement.setString(4, game_name);
            ResultSet resultSet = statement.executeQuery();

            SQLTrainCard card = null;
            if(resultSet.next()) {
                card = SQLTrainCard.buildTrainCardFromResultSet(resultSet);
            }
            return new TrainCard(Color.getTrainCardColorFromString(card.getTrainType()));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public DestinationCard drawRandomDestinationCard(String game_name, String player_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("UPDATE %1$s SET %8$s = ?,\n" +
                            "%6$s = (SELECT %9$s FROM %10$s WHERE %11$s = ?)\n" +
                            "WHERE %12$s = (SELECT %12$s FROM %1$s\n" +
                            "WHERE %2$s = (SELECT %3$s FROM %4$s WHERE %5$s = ?)\n" +
                            "AND %6$s IS NULL\n" +
                            "AND %7$s = ?\n" +
                            "ORDER BY random() LIMIT 1) RETURNING *;",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,

                    SQLGame.GAME_NAME,
                    SQLDestinationCard.PLAYER_ID,
                    SQLDestinationCard.DISCARDED,
                    SQLDestinationCard.DRAWN,

                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,
                    SQLDestinationCard.ID);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setBoolean(1, true);
            statement.setString(2, player_name);
            statement.setString(3, game_name);
            statement.setBoolean(4, false);
            ResultSet resultSet = statement.executeQuery();

            SQLDestinationCard card = null;
            if(resultSet.next()) {
                card = SQLDestinationCard.buildDestinationCardFromResultSet(resultSet);
            }
            else if(reshuffleDestinationCardDiscardPile(game_name)) {
                resultSet = statement.executeQuery();
                card = SQLDestinationCard.buildDestinationCardFromResultSet(resultSet);
            }
            return new DestinationCard(getCity(card.getCity1()), getCity(card.getCity2()), card.getPoints());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private boolean reshuffleTrainCardDiscardPile(String game_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("UPDATE %1$s SET %2$s = false\n" +
                            "WHERE %3$s = (SELECT %4$s FROM %5$s WHERE %6$s = ?)",
                    SQLTrainCard.TABLE_NAME,
                    SQLTrainCard.DISCARDED,
                    SQLTrainCard.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            return (statement.executeUpdate() > 0);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean reshuffleDestinationCardDiscardPile(String game_name) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("UPDATE %1$s SET %2$s = false\n" +
                            "WHERE %3$s = (SELECT %4$s FROM %5$s WHERE %6$s = ?)",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.DISCARDED,
                    SQLDestinationCard.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            return (statement.executeUpdate() > 0);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public List<BaseCommand> getCommandsSinceLastCommand(String game_name, String player_name, int lastCommandID) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("(SELECT * FROM %1$s natural join participants where user_id=player_id\n" +
                            " and %2$s = (SELECT %3$s FROM %4$s WHERE %5$s = ?)\n" +
                            " AND %12$s >= ? order by command_number);",
                    SQLCommand.TABLE_NAME,

                    SQLCommand.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,

                    SQLCommand.PLAYER_ID,
                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,

                    SQLCommand.VISIBLE_TO_SELF,
                    SQLCommand.VISIBLE_TO_ALL,
                    SQLCommand.COMMAND_NUMBER
            );

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setInt(2, lastCommandID);

            ResultSet resultSet = statement.executeQuery();
            return getCommandsFromResultSet(resultSet, player_name);
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<BaseCommand> getCommandsFromResultSet(ResultSet resultSet, String player_name) throws SQLException {
        List<BaseCommand> commands = new ArrayList<>();

        while(resultSet.next()) {
            commands.add(SQLCommand.buildCommandFromResultSet(resultSet, player_name));
        }

        return commands;
    }

    @Override
    public void removeTrainsFromPlayer(String game_name, String username, int trainsToRemove) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("update participants set trains_left = ?\n" +
                    "where game_id in (select game_id from game where name = ?)\n" +
                    "and user_id in (select user_id from player where username = ?)\n");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, numTrainsLeft(game_name, username) - trainsToRemove);
            statement.setString(2, game_name);
            statement.setString(3, username);

            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean canClaimRoute(String game_name, String username, int route_number) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("WITH route_train_requirement AS\n" +
                    "\t(SELECT route_length FROM route WHERE route_number = ?),\n" +
                    "route_owner AS (SELECT player_id FROM claimed_route\n" +
                    "\tWHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "\tAND route_id = ?)\n" +
                    "SELECT trains_left, route_length, player_id FROM participants LEFT OUTER JOIN route_owner ON (participants.user_id = route_owner.player_id), route_train_requirement\n" +
                    "WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "AND user_id IN (SELECT user_id FROM player WHERE username = ?)");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, route_number);
            statement.setString(2, game_name);
            statement.setInt(3, route_number);
            statement.setString(4, game_name);
            statement.setString(5, username);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                int trains_left = resultSet.getInt(SQLParticipants.TRAINS_LEFT);
                int route_owner = resultSet.getInt(SQLClaimedRoute.PLAYER_ID);
                if(resultSet.wasNull()) {
                    route_owner = -1;
                }
                int route_length = resultSet.getInt(SQLRoute.ROUTE_LENGTH);

                return trains_left > route_length && route_owner == -1 &&!isClaimedDouble(game_name, username, route_number);
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean claimRoute(String game_name, String username, int route_number) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("" +
                            "insert into claimed_route (player_id, game_id, route_id) values \n" +
                            "((SELECT user_id FROM player WHERE username = ?),\n" +
                            "(SELECT game_id FROM game WHERE name = ?),\n" +
                            "?)\n",
                    SQLClaimedRoute.TABLE_NAME,

                    SQLClaimedRoute.PLAYER_ID,
                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,

                    SQLClaimedRoute.GAME_ID,
                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,

                    SQLClaimedRoute.ROUTE_ID);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, username);
            statement.setString(2, game_name);
            statement.setInt(3, route_number);
            int numUpdated = statement.executeUpdate();
            return (numUpdated == 1);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private City getCity(int cityID) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT * FROM %1$s WHERE %2$s = ?",
                    SQLCity.TABLE_NAME,
                    SQLCity.ID);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, cityID);

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                String id = resultSet.getString(SQLCity.ID);
                String name = resultSet.getString(SQLCity.NAME);
                int x_coord = resultSet.getInt(SQLCity.POINT_X);
                int y_coord = resultSet.getInt(SQLCity.POINT_Y);

                return new City(x_coord, y_coord, name);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private boolean addFirstCommand(BaseCommand cmd, boolean visibleToSelf, boolean visibleToAll) {
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("INSERT INTO %1$s(%14$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s) VALUES (\n" +
                            "   ?,\n" +
                            "   (SELECT %8$s FROM %9$s WHERE %10$s = ?\n" +
                            "       AND NOT EXISTS (SELECT * FROM %1$s WHERE %14$s = ?\n" +
                            "           AND %2$s = (SELECT %8$s FROM %9$s WHERE %10$s = ?))),\n" +
                            "   (SELECT %11$s FROM %12$s WHERE %13$s = ?),\n" +
                            "   ?,\n ?,\n ?,\n ?);",
                    SQLCommand.TABLE_NAME,
                    SQLCommand.GAME_ID,
                    SQLCommand.PLAYER_ID,
                    SQLCommand.COMMAND_TYPE,
                    SQLCommand.METADATA,
                    SQLCommand.VISIBLE_TO_SELF,
                    SQLCommand.VISIBLE_TO_ALL,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,

                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,

                    SQLCommand.COMMAND_NUMBER);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, cmd.getCommandNumber());
            statement.setString(2, cmd.getGameName());
            statement.setInt(3, cmd.getCommandNumber());
            statement.setString(4, cmd.getGameName());
            statement.setString(5, cmd.getPlayerName());
            statement.setString(6, cmd.getName());
            statement.setString(7, Serializer.serialize(cmd));
            statement.setBoolean(8, visibleToSelf);
            statement.setBoolean(9, visibleToAll);

            return statement.executeUpdate() == 1;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public synchronized boolean addCommand(BaseCommand cmd, boolean visibleToSelf, boolean visibleToAll) {
        if(cmd.getCommandNumber() == 0) {
            return addFirstCommand(cmd, visibleToSelf, visibleToAll);
        }
        try(Connection connection = session.getConnection()) {
            String sqlString = String.format("INSERT INTO %1$s(%14$s,%2$s,%3$s,%4$s,%5$s,%6$s,%7$s) VALUES (\n" +
                            "   ?,\n" +
                            "   (SELECT %8$s FROM %9$s WHERE %10$s = ?" +
                            "       AND EXISTS (SELECT * FROM %1$s WHERE %14$s = ?\n" +
                            "           AND %2$s = (SELECT %8$s FROM %9$s WHERE %10$s = ?))),\n" +
                            "   (SELECT %11$s FROM %12$s WHERE %13$s = ?),\n" +
                            "   ?,\n ?,\n ?,\n ?);",
                    SQLCommand.TABLE_NAME,
                    SQLCommand.GAME_ID,
                    SQLCommand.PLAYER_ID,
                    SQLCommand.COMMAND_TYPE,
                    SQLCommand.METADATA,
                    SQLCommand.VISIBLE_TO_SELF,
                    SQLCommand.VISIBLE_TO_ALL,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,

                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,

                    SQLCommand.COMMAND_NUMBER);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, cmd.getCommandNumber());
            statement.setString(2, cmd.getGameName());
            statement.setInt(3, cmd.getCommandNumber() - 1);
            statement.setString(4, cmd.getGameName());
            statement.setString(5, cmd.getPlayerName());
            statement.setString(6, cmd.getName());
            statement.setString(7, Serializer.serialize(cmd));
            statement.setBoolean(8, visibleToSelf);
            statement.setBoolean(9, visibleToAll);

            return statement.executeUpdate() == 1;
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean returnDestCards(String gameName, String playerName, List<DestinationCard> destinationCards) {
        if(destinationCards.size() == 1 && getDrawnDestCardCount(gameName, playerName) == 3) {
            return returnSingleDestCard(gameName, playerName, destinationCards.get(0));
        }
        else if(destinationCards.size() == 2
                && getDrawnDestCardCount(gameName, playerName) == 3
                && getDestinationCardCount(playerName) > 3){
            try (Connection connection = session.getConnection()) {
                String sqlString = String.format("UPDATE %1$s SET %2$s = null, %3$s = ?, %4$s = ?\n" +
                                "WHERE %2$s = (SELECT %5$s FROM %6$s WHERE %7$s = ?)\n" +
                                "AND %8$s = (SELECT %9$s FROM %10$s WHERE %11$s = ?)\n" +
                                "AND ((%12$s = (SELECT %14$s FROM %15$s WHERE %16$s = ?)\n" +
                                "   AND %13$s = (SELECT %14$s FROM %15$s WHERE %16$s = ?))\n" +
                                "OR (%12$s = (SELECT %14$s FROM %15$s WHERE %16$s = ?)\n" +
                                "   AND %13$s = (SELECT %14$s FROM %15$s WHERE %16$s = ?)))\n" +
                                "AND %3$s = ?;\n" +
                                "UPDATE %1$s SET %3$s = ?\n" +
                                "WHERE %2$s = (SELECT %5$s FROM %6$s WHERE %7$s = ?)\n" +
                                "AND %8$s = (SELECT %14$s FROM %15$s WHERE %16$s = ?);\n",
                        SQLDestinationCard.TABLE_NAME,
                        SQLDestinationCard.PLAYER_ID,
                        SQLDestinationCard.DRAWN,
                        SQLDestinationCard.DISCARDED,

                        SQLPlayer.ID, //5
                        SQLPlayer.TABLE_NAME,
                        SQLPlayer.USERNAME,

                        SQLDestinationCard.GAME_ID, //8

                        SQLGame.ID, //9
                        SQLGame.TABLE_NAME,
                        SQLGame.GAME_NAME,

                        SQLDestinationCard.CITY_1, //12
                        SQLDestinationCard.CITY_2,

                        SQLCity.ID, //14
                        SQLCity.TABLE_NAME,
                        SQLCity.NAME);
                PreparedStatement statement = connection.prepareStatement(sqlString);
                statement.setBoolean(1, false);
                statement.setBoolean(2, true);
                statement.setString(3, playerName);
                statement.setString(4, gameName);
                statement.setString(5, destinationCards.get(0).getCity1().getName());
                statement.setString(6, destinationCards.get(0).getCity2().getName());
                statement.setString(7, destinationCards.get(1).getCity1().getName());
                statement.setString(8, destinationCards.get(1).getCity2().getName());
                statement.setBoolean(9, true);
                statement.setBoolean(10, false);
                statement.setString(11, playerName);
                statement.setString(12, gameName);

                return statement.executeUpdate() == 2;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    private boolean returnSingleDestCard(String gameName, String playerName, DestinationCard destinationCard) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("UPDATE %1$s SET %2$s = null, %3$s = ?, %4$s = ?\n" +
                            "WHERE %5$s = (SELECT %6$s FROM %7$s WHERE %8$s = ?)\n" +
                            "AND %2$s = (SELECT %9$s FROM %10$s WHERE %11$s = ?)\n" +
                            "AND %12$s = (SELECT %14$s FROM %15$s WHERE %16$s = ?)" +
                            "AND %13$s = (SELECT %14$s FROM %15$s WHERE %16$s = ?)" +
                            "AND %3$s = ?;" +
                            "UPDATE %1$s SET %3$s = ?\n" +
                            "WHERE %5$s = (SELECT %6$s FROM %7$s WHERE %8$s = ?)\n" +
                            "AND %2$s = (SELECT %9$s FROM %10$s WHERE %11$s = ?);\n",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.PLAYER_ID,
                    SQLDestinationCard.DRAWN,
                    SQLDestinationCard.DISCARDED,
                    SQLDestinationCard.GAME_ID,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,

                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,

                    SQLDestinationCard.CITY_1,
                    SQLDestinationCard.CITY_2,

                    SQLCity.ID,
                    SQLCity.TABLE_NAME,
                    SQLCity.NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setBoolean(1, false);
            statement.setBoolean(2, true);
            statement.setString(3, gameName);
            statement.setString(4, playerName);
            statement.setString(5, destinationCard.getCity1().getName());
            statement.setString(6, destinationCard.getCity2().getName());
            statement.setBoolean(7, true);
            statement.setBoolean(8, false);
            statement.setString(9, gameName);
            statement.setString(10, playerName);

            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private int getDrawnDestCardCount(String gameName, String playerName) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT * FROM %1$s\n" +
                            "WHERE %2$s = (SELECT %6$s FROM %7$s WHERE %8$s = ?)\n" +
                            "AND %3$s = (SELECT %9$s FROM %10$s WHERE %11$s = ?)\n" +
                            "AND %4$s = ?" +
                            "AND %5$s = ?",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.PLAYER_ID,
                    SQLDestinationCard.GAME_ID,
                    SQLDestinationCard.DISCARDED,
                    SQLDestinationCard.DRAWN,

                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, playerName);
            statement.setString(2, gameName);
            statement.setBoolean(3, false);
            statement.setBoolean(4, true);

            ResultSet resultSet = statement.executeQuery();

            int drawnCardCount = 0;
            while(resultSet.next()) {
                ++drawnCardCount;
            }
            return drawnCardCount;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @Override
    public List<DestinationCard> getPlayerDestinationCards(String game_name, String player_name) {
        List<DestinationCard> playerDestCards = new ArrayList<>();
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT *\n" +
                            "FROM %1$s\n" +
                            "WHERE %2$s IN (SELECT %3$s FROM %4$s WHERE %5$s = ?)\n" +
                            "AND %6$s IN (SELECT %7$s FROM %8$s\n" +
                            "   WHERE %9$s = (SELECT user_id FROM player WHERE username = ?)\n" +
                            "   AND %10$s = (SELECT %11$s FROM %12$s WHERE %13$s = ?))",
                    SQLDestinationCard.TABLE_NAME,
                    SQLDestinationCard.GAME_ID,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,

                    SQLDestinationCard.PLAYER_ID,

                    SQLParticipants.USER_ID,
                    SQLParticipants.TABLE_NAME,
                    SQLParticipants.PLAYER_NUMBER,
                    SQLParticipants.GAME_ID,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setString(2, player_name);
            statement.setString(3, game_name);

            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                SQLDestinationCard destinationCard = SQLDestinationCard.buildDestinationCardFromResultSet(resultSet);
                City city1 = getCity(destinationCard.getCity1());
                City city2 = getCity(destinationCard.getCity2());
                playerDestCards.add(new DestinationCard(city1, city2, destinationCard.getPoints()));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return playerDestCards;
    }

    @Override
    public boolean postMessage(String game_name, String player_name, String message) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("INSERT INTO %1$s(%2$s, %3$s, %4$s)" +
                            "VALUES ((SELECT %5$s FROM %6$s WHERE %7$s = ?)," +
                            "(SELECT %8$s FROM %9$s WHERE %10$s = ?), ?)",
                    SQLMessage.TABLE_NAME,
                    SQLMessage.GAME_ID,
                    SQLMessage.PLAYER_ID,
                    SQLMessage.MESSAGE,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME,

                    SQLPlayer.ID,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.USERNAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setString(2, player_name);
            statement.setString(3, message);

            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Message> getMessages(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT * FROM %1$s\n" +
                            "WHERE %2$s = (SELECT %3$s FROM %4$s WHERE %5$s = ?);",
                    SQLMessage.TABLE_NAME,
                    SQLMessage.GAME_ID,

                    SQLGame.ID,
                    SQLGame.TABLE_NAME,
                    SQLGame.GAME_NAME);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);

            ResultSet resultSet = statement.executeQuery();

            List<Message> messages = new ArrayList<>();
            while(resultSet.next()) {
                SQLMessage sqlMessage = SQLMessage.parseResultSetRow(resultSet);
                messages.add(new Message(getUsername(sqlMessage.getPlayerID()), sqlMessage.getMessage()));
            }

            return messages;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String getUsername(String player_id) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT %1$s FROM %2$s\n" +
                            "WHERE %3$s = ?;",
                    SQLPlayer.USERNAME,
                    SQLPlayer.TABLE_NAME,
                    SQLPlayer.ID);

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, Integer.parseInt(player_id));

            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getString(SQLPlayer.USERNAME);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean discardCard(String gameName, String playerName, Color color) {
        try (Connection connection = session.getConnection()) {
            color.toString();
            String sqlString = String.format("" +
                    "with card as (select * from train_card\n" +
                    "      where game_id in (select game_id from game where name=?)\n" +
                    "      and player_id in (select user_id from player where username=?)\n" +
                    "      and train_type=?\n" +
                    "      Limit 1\n" +
                    "      FOR UPDATE)\n" +
                    "    update train_card t set discarded = true, player_id = null\n" +
                    "      from card\n" +
                    "      where t.train_card_id = card.train_card_id\n" +
                    "      and t.game_id = card.game_id");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, gameName);
            statement.setString(2, playerName);
            statement.setString(3, color.toString().toLowerCase());
            return statement.executeUpdate() == 1;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean allHandsInitialized(String gameName) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format(
                    "with initialize as (\n" +
                            "select count(*) as count from command where game_id = (select game_id from game where name=?) and command_type='InitializeHand'\n" +
                            "), returned as (\n" +
                            "select count(*) as count from command where game_id = (select game_id from game where name=?) and command_type='ReturnDestCards'\n" +
                            ")\n" +
                            "select (i.count<=r.count AND i.count > 0) from initialize as i, returned as r"
            );
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, gameName);
            statement.setString(2, gameName);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                return resultSet.getBoolean(1);
            }
        } catch( Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public EndTurnCommand getEndTurnCommand(String gameName, int commandNumber, String playerName) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("with current_player as (select * from participants where \n" +
                    "\tgame_id in (select game_id from game where name=?)\n" +
                    "\tand user_id in (select user_id from player where username=? limit 1)\n" +
                    "), max_player as (select max(player_number) as max from participants\n" +
                    "    where game_id in (select game_id from game where name=?))\n" +
                    "select player_number as previous, mod(player_number + 1,max + 1) as next from  max_player, current_player\n");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, gameName);
            statement.setString(2, playerName);
            statement.setString(3, gameName);
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                EndTurnCommand command = new EndTurnCommand();
                command.setPlayerName(playerName);
                command.setGameName(gameName);
                command.setCommandNumber(commandNumber);
                command.setNextPlayer(resultSet.getInt("next"));
                int previous = resultSet.getInt("previous");
                command.setPreviousPlayer(previous);
                command.setPlayerNumber(previous);
                return command;
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        EndTurnCommand res = new EndTurnCommand();
        res.setPlayerName(playerName);
        res.setCommandNumber(commandNumber);
        res.setPreviousPlayer(-1);
        res.setNextPlayer(0);
        res.setGameName(gameName);
        res.setPlayerName(playerName);
        return res;

    }

    @Override
    public List<Color> getSlotCardColors(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT * FROM train_card\n" +
                    "WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "AND slot IS NOT NULL\n" +
                    "ORDER BY slot ASC;");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);

            ResultSet resultSet = statement.executeQuery();

            List<Color> slotCards = new ArrayList<>();
            while(resultSet.next()) {
                slotCards.add(Color.getTrainCardColorFromString(resultSet.getString(SQLTrainCard.TRAIN_TYPE)));
            }
            return slotCards;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean validateCommand(BaseCommand command) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT * FROM command\n" +
                    "WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "ORDER BY command_number DESC;");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, command.getGameName());
            ResultSet resultSet = statement.executeQuery();

            if(resultSet.next()) {
                int lastCommandNumber = resultSet.getInt(SQLCommand.COMMAND_NUMBER);
                return command.getCommandNumber() == 0 || command.getCommandNumber() == 1 + lastCommandNumber;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean hasDrawnTwoTrainCards(String game_name, String player_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("SELECT * FROM command\n" +
                    "INNER JOIN player ON command.player_id = player.user_id\n" +
                    "WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "ORDER BY command_number DESC\n" +
                    "LIMIT 2;");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            ResultSet resultSet = statement.executeQuery();

            while(resultSet.next()) {
                String username = resultSet.getString(SQLPlayer.USERNAME);
                String command_type = resultSet.getString(SQLCommand.COMMAND_TYPE);
                if(!username.equals(player_name) || !command_type.equals("DrawTrainCard")) {
                    return false;
                }
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    /**
     *
     * @param game_name
     * @return number of drawCard commands since last DrawTrainCard command
     */
    @Override
    public int getNumberOfDrawTrainCommands(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("" +
                    "with game_commands as (select * from command where game_id IN (SELECT game_id FROM game WHERE name = ?))\n" +
                    "        select count(*) from game_commands where command_type='DrawTrainCard' and command_number > (select max(command_number) from game_commands where command_type='EndTurn')");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt(1);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int numTrainsLeft(String game_name, String player_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("" +
                    "select trains_left from participants where game_id in \n" +
                    "(SELECT game_id FROM game WHERE name = ?)\n" +
                    "and user_id in (select user_id from player where username=?)");
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setString(2, player_name);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt(1);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean isEndOfGame(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format(
                    "with last_turn_command as \n" +
                            "\t(select * from command where game_id in \n" +
                            "     \t(select game_id from game where name = ?) \n" +
                            "     \t\tand command_type='LastTurn' order by command_number limit 1)\n" +
                            "select count(*) from command WHERE game_id =(select game_id from last_turn_command) \n" +
                            "and player_id=(select player_id from last_turn_command) \n" +
                            "and command_number > (select command_number from last_turn_command)"
            );
            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            ResultSet resultSet = statement.executeQuery();
            if(resultSet.next()){
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isClaimedDouble(String game_name, String player_name, int route_number) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("WITH route_to_claim AS (\n" +
                    "                       SELECT route_number, city_1, city_2 FROM route\n" +
                    "                       WHERE route_number = ?\n" +
                    "                    ),\n" +
                    "duplicate_track AS (SELECT route.route_number FROM route, route_to_claim\n" +
                    "                    WHERE route.city_1 = route_to_claim.city_1\n" +
                    "                    AND route.city_2 = route_to_claim.city_2\n" +
                    "                    AND route_to_claim.route_number != route.route_number)\n" +
                    "SELECT * FROM route INNER JOIN claimed_route ON (route.route_number = claimed_route.route_id), duplicate_track\n" +
                    "\tWHERE route.route_number = duplicate_track.route_number\n" +
                    "    AND player_id IN (SELECT user_id FROM player WHERE username = ?)\n" +
                    "    AND game_id IN (SELECT game_id FROM game WHERE name = ?);");

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setInt(1, route_number);
            statement.setString(2, player_name);
            statement.setString(3, game_name);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }

    @Override
    public void redealSlotCards(String game_name) {
        clearSlots(game_name);
        initializeSlots(game_name);
    }

    private void clearSlots(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("" +
                    "UPDATE train_card SET slot = NULL, discarded = true\n" +
                    "WHERE slot IS NOT NULL\n" +
                    "AND game_id IN (SELECT game_id FROM game WHERE name = ?)");

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean alreadyLastRound(String game_name) {
        try (Connection connection = session.getConnection()) {
            String sqlString = String.format("" +
                    "SELECT * FROM command\n" +
                    "WHERE game_id IN (SELECT game_id FROM game WHERE name = ?)\n" +
                    "AND command_type = ?");

            PreparedStatement statement = connection.prepareStatement(sqlString);
            statement.setString(1, game_name);
            statement.setString(2, "LastTurn");
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
