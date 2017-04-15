package edu.goldenhammer.database;

import edu.goldenhammer.model.*;
import edu.goldenhammer.server.commands.BaseCommand;
import edu.goldenhammer.server.commands.EndTurnCommand;

import java.util.List;

public interface IDatabaseController {
/*
Player registration and login
 */

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
     * @param gameID
     * @return
     */
    List<String> getPlayers(String gameID);

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

    /*
    Game management
     */

    /**
     *
     * @param name
     * @return
     */
    Boolean createGame(String name);

    /**
     *
     * @return
     */
    GameList getGames();

    /**
     *
     * @param player
     * @return
     */
    GameList getGames(String player);

    /**
     *
     * @param player
     * @param gameID
     * @return
     */
    Boolean joinGame(String player, String gameID);

    /**
     *
     * @param player
     * @param gameID
     * @return
     */
    Boolean leaveGame(String player, String gameID);

    /**
     *
     * @param gameName
     */
    void maybeDropGame(String gameName);

    /*
    Starting the game
     */

    /**
     *
     * @param gameID
     * @return
     */
    IGameModel playGame(String gameID);

    /**
     *
     * @param gameName
     * @return
     */
    boolean allHandsInitialized(String gameName);

    /*
    Train cards
     */

    /**
     *
     * @param gameName
     * @param playerName
     * @return
     */
    TrainCard drawRandomTrainCard(String gameName, String playerName);

    /**
     * @param game_name
     * @param player_name
     * @param slot
     * @return
     */
    TrainCard drawTrainCardFromSlot(String game_name, String player_name, int slot);

    /**
     *
     * @param game_name
     * @param player_name
     * @return
     */
    boolean hasDrawnTwoTrainCards(String game_name, String player_name);

    /**
     *
     * @param game_name
     */
    void redealSlotCards(String game_name);

    /**
     *
     * @param gameName
     * @param playerName
     * @param color
     * @return
     */
    boolean discardCard(String gameName, String playerName, Color color);

    /**
     *
     * @param game_name
     * @return
     */
    List<Color> getSlotCardColors(String game_name);

    /*
    Destination cards
     */

    /**
     *
     * @param gameName
     * @param playerName
     * @return
     */
    DestinationCard drawRandomDestinationCard(String gameName, String playerName);

    /**
     *
     * @param game_name
     * @param player_name
     * @return
     */
    List<DestinationCard> getPlayerDestinationCards(String game_name, String player_name);

    /**
     *
     * @param gameName
     * @param playerName
     * @param destinationCards
     * @return
     */
    boolean returnDestCards(String gameName, String playerName, List<DestinationCard> destinationCards);

    /*
    Messages
     */

    /**
     *
     * @param game_name
     * @param player_name
     * @param message
     * @return
     */
    boolean postMessage(String game_name, String player_name, String message);

    /**
     *
     * @param game_name
     * @return
     */
    List<Message> getMessages(String game_name);

    /*
    Laying tracks
     */

    /**
     *
     * @param game_name
     * @param username
     * @param route_number
     * @return
     */
    boolean canClaimRoute(String game_name, String username, int route_number);

    /**
     *
     * @param game_name
     * @param username
     * @param route_number
     * @return
     */
    boolean claimRoute(String game_name, String username, int route_number);

    /**
     *
     * @param game_name
     * @param username
     * @param trainsToRemove
     */
    void removeTrainsFromPlayer(String game_name, String username, int trainsToRemove);

    /**
     *
     * @param game_name
     * @return
     */
    List<Track> getTracks(String game_name);

    /**
     *
     * @param game_name
     * @param player_name
     * @return
     */
    int numTrainsLeft(String game_name, String player_name);

    /*
    Commands
     */

    /**
     *
     * @param cmd
     * @param visibleToSelf
     * @param visibleToAll
     * @return
     */
    boolean addCommand(BaseCommand cmd, boolean visibleToSelf, boolean visibleToAll);

    /**
     *
     * @param gameName
     * @param commandNumber
     * @param playerName
     * @return
     */
    EndTurnCommand getEndTurnCommand(String gameName, int commandNumber, String playerName);

    /**
     *
     * @param game_name
     * @param player_name
     * @param lastCommandID
     * @return
     */
    List<BaseCommand> getCommandsSinceLastCommand(String game_name, String player_name, int lastCommandID);

    /**
     *
     * @param command
     * @return
     */
    boolean validateCommand(BaseCommand command);

    /**
     *
     * @param game_name
     * @return
     */
    int getNumberOfDrawTrainCommands(String game_name);

    /*
    End game
     */

    /**
     *
     * @param game_name
     * @return
     */
    boolean isEndOfGame(String game_name);

    /**
     *
     * @param game_name
     * @return
     */
    boolean alreadyLastRound(String game_name);

    /*
    Getting the game model
     */

    /**
     *
     * @param game_name
     * @return
     */
    GameModel getGameModel(String game_name);
}
