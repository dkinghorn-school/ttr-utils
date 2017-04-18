package edu.goldenhammer.database;

import edu.goldenhammer.model.Player;

import java.util.List;

/**
 * Created by McKean on 4/17/2017.
 */

public interface IUserDAO {

    /**
     * Gets all the authentication information to return when logging in to the game, including username and access token,
     * contained in a Player object
     *
     * @param player the player's username
     * @return a Player object containing the username and access token for the given user.
     */
    Player getPlayerInfo(String player);

    /**
     *
     * @param username
     * @param password
     * @return
     */
    Boolean login(String username, String password);

    /**
     *
     * @param username
     * @param password
     * @return
     */
    Boolean createUser(String username, String password);

    /**
     *
     * @param username
     * @param accessToken
     */
    void setAccessToken(String username, String accessToken);
}
