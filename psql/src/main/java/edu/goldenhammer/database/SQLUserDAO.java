package edu.goldenhammer.database;

import edu.goldenhammer.database.postgresql.SQLConnectionFactory;
import edu.goldenhammer.database.postgresql.data_types.SQLPlayer;
import edu.goldenhammer.model.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by McKean on 4/17/2017.
 */

public class SQLUserDAO implements IUserDAO{
    private SQLConnectionFactory session;

    public SQLUserDAO() {
        initializeDatabase();
    }

    private void initializeDatabase() {
        this.session = SQLConnectionFactory.getInstance();
        ensureTableCreated();
    }

    private void ensureTableCreated() {
        createTable(SQLPlayer.CREATE_STMT);
    }

    private void createTable(String sqlStatementString) {
        try (Connection connection = session.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sqlStatementString);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear() {

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
}
