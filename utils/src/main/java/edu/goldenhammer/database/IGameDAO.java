package edu.goldenhammer.database;

import edu.goldenhammer.model.*;
import edu.goldenhammer.server.commands.BaseCommand;
import edu.goldenhammer.server.commands.EndTurnCommand;

import java.util.List;

/**
 * Created by McKean on 4/17/2017.
 */

public interface IGameDAO {
     /*
    Game management
     */


     void clear();
     void setCheckpointLength(int checkpointLength);
    void setMaxTrains(int maxTrains);
    /**
     * Get an ArrayList of the usernames of the players in the given game
     * @param gameID
     * @return
     */
    List<String> getPlayers(String gameID);

    /**
     *
     * @param name
     * @return
     */
    Boolean createGame(String name);

    /**
     *
     * @return a GameList object that contains a List of all the GameListItems, containing
     * the game's name, whether or not it has started, and a List of the usernames of the players
     * in the game.
     */
    GameList getGames();

    /**
     *
     * @param player
     * @return the same thing as getGames(), but only for the games that have the given player
     */
    GameList getGames(String player);

    /**
     * Adds the given player to the given game as long as the following conditions are met:
     * -the game has not started yet
     * -the game exists
     * -the input username already exists as a player
     * @param username
     * @param game_name
     * @return whether the player was added to the game
     */
    Boolean joinGame(String username, String game_name);

    /**
     * Removes the given player from the given game as long as:
     * -the game has not started yet
     * -the player is currently part of the game
     * -the game and player both exist
     * @param username
     * @param game_name
     * @return whether the player successfully left the game
     */
    Boolean leaveGame(String username, String game_name);

    /**
     * Deletes the game if it has no participants
     * @param gameName
     */
    void maybeDropGame(String gameName);

    /*
    Starting the game
     */

    /**
     * If the game has not been started yet, initializes the game. You must do the following to initialize a game:
     * -set everyone's points to zero and trains_left to the maximum number of trains
     * -create all the cities from cities.txt
     * -create all the routes, starting with no owner, from routes.txt
     * -create all the destination cards from destinations.txt, initialized to being in the deck with no owner
     * -create all the train cards, initialized to being in the deck with no owner
     * -create and execute an InitializeHandCommand for each player, making sure to set the command number correctly
     *  and making sure none of the drawn cards come from the discard pile or any of the five slots
     * -draw five random train cards and set them to the five available slots, making sure that the slot
     *  cards are not discarded and have not yet been drawn
     * -start the game
     * Constructs and returns the game model
     * @param game_name
     * @return the game, minus all the commands that have not yet operated on the stored model
     */
    IGameModel playGame(String game_name);

    /**
     * Looks back through the commands to see if each participant has generated both an InitializeHandCommand
     *  and a ReturnDestCardsCommand
     * @param gameName
     * @return whether all hands have been initialized
     */
    boolean allHandsInitialized(String gameName);

    /*
    Train cards
     */

    /**
     * Selects a random train card from the deck and assigns it to the given player
     * @param gameName
     * @param playerName
     * @return the TrainCard drawn
     */
    TrainCard drawRandomTrainCard(String gameName, String playerName);

    /**
     * Selects the train card from the given slot and assigns it to the given player. Replaces the drawn train
     *  card with a new random one from the deck. If the deck is empty, the discard pile is reshuffles and we try
     *  again to draw a card. If that still doesn't work, we assume all the train cards have been drawn, and we
     *  assign the train card to the player without replacing it.
     * @param game_name
     * @param player_name
     * @param slot
     * @return the TrainCard drawn
     */
    TrainCard drawTrainCardFromSlot(String game_name, String player_name, int slot);

    /**
     * Checks if the last two commands a player has given have been DrawTrainCard commands
     * @param game_name
     * @param player_name
     * @return whether the given player has drawn two train cards this turn
     */
    boolean hasDrawnTwoTrainCards(String game_name, String player_name);

    /**
     * To be called after a train card has been drawn from a slot if there are three wilds in the slots now.
     *  Discards all current slot cards and draws five more from the deck. If there are not five cards left,
     *  we deal as many as possible then shuffle the deck and deal as many as possible.
     * @param game_name
     */
    void redealSlotCards(String game_name);

    /**
     * Takes a card of the given color from the given player, deletes the ownership, and sends the card to the
     *  discard pile.
     * @param gameName
     * @param playerName
     * @param color
     * @return whether the card has been successfully discarded
     */
    boolean discardCard(String gameName, String playerName, Color color);

    /**
     * Gets all the slot cards in the game
     * @param game_name
     * @return a list of the slot cards, in order
     */
    List<Color> getSlotCardColors(String game_name);

    /*
    Destination cards
     */

    /**
     * Gets a random destination card from the deck and assigns it to the given player. The destination
     *  card will have been drawn but not fully assigned: we need to wait for the player to select between
     *  zero and two destination cards to discard before assigning it to the player for good.
     * @param gameName
     * @param playerName
     * @return the drawn DestinationCard
     */
    DestinationCard drawRandomDestinationCard(String gameName, String playerName);

    /**
     * Generates a List of all the DestinationCards that belong to the given player
     * @param game_name
     * @param player_name
     * @return all the DestinationCards the player owns
     */
    List<DestinationCard> getPlayerDestinationCards(String game_name, String player_name);

    /**
     * If this is the first turn, only allows the player to return zero or one destination card. Otherwise,
     *  a player can return between zero and two cards. Makes sure that the cards in the list belong to the
     *  current player and have been drawn, but not assigned. Then removes the player's ownership from the
     *  cards and sends them to the discard pile. All other drawn-but-not-assigned destination cards belonging
     *  to the player are sent to the player's hand; the drawn-but-not-assigned tag is removed.
     * @param gameName
     * @param playerName
     * @param destinationCards
     * @return whether the cards were successfully discarded
     */
    boolean returnDestCards(String gameName, String playerName, List<DestinationCard> destinationCards);

    /*
    Messages
     */

    /**
     * Adds a message to the in-game chat
     * @param game_name
     * @param player_name
     * @param message
     * @return whether the message was successfully posted
     */
    boolean postMessage(String game_name, String player_name, String message);

    /**
     * Generates a list of all the messages in the given game
     * @param game_name
     * @return a list of all the game's messages
     */
    List<Message> getMessages(String game_name);

    /*
    Laying tracks
     */

    /**
     * Checks to make sure the a route can be claimed, according to the following criteria:
     * -The route has not yet been claimed
     * -If the route is a double, the player doesn't own the other double route
     * -The player has enough trains left to lay the track
     * @param game_name
     * @param username
     * @param route_number
     * @return whether the player can claim the route
     */
    boolean canClaimRoute(String game_name, String username, int route_number);

    /**
     * Assigns the route to the player with no extra checking
     * @param game_name
     * @param username
     * @param route_number
     * @return whether the player has successfully claimed the route
     */
    boolean claimRoute(String game_name, String username, int route_number);

    /**
     * Decrements the number of trains owned by the player by the number given in trainsToRemove
     * @param game_name
     * @param username
     * @param trainsToRemove
     */
    void removeTrainsFromPlayer(String game_name, String username, int trainsToRemove);

    /**
     * Gets all the tracks and their associated data, including who owns them and what cities they connect.
     *  Look in the Track object for all the data you need to find.
     * @param game_name
     * @return A list of all the tracks in the game, fully populated with data
     */
    List<Track> getTracks(String game_name);

    /**
     * Finds how many trains the player has left in the game
     * @param game_name
     * @param player_name
     * @return the number of trains the player has left
     */
    int numTrainsLeft(String game_name, String player_name);

    /*
    Commands
     */

    /**
     * Adds the command to the database, according to the following criteria:
     * -If the command number is zero, it will be instantly added to the database only if
     *  there is no currently-existing command with the number zero
     * -If the command number is greater than zero, it will only be added if the command number
     *  doesn't exist yet, but the previous command number does exist.
     * @param cmd
     * @param visibleToSelf
     * @param visibleToAll
     * @return whether the command was added to the database
     */
    boolean addCommand(BaseCommand cmd, boolean visibleToSelf, boolean visibleToAll);

    /**
     * Generates (but does not execute or store in the database) an EndTurnCommand for the given player. Finds
     * the previous and next players to store in the command.
     * @param gameName
     * @param commandNumber
     * @param playerName
     * @return the generated EndTurnCommand
     */
    EndTurnCommand getEndTurnCommand(String gameName, int commandNumber, String playerName);

    /**
     * Gets a list of all the commands generated since the given command number
     * @param game_name
     * @param player_name
     * @param lastCommandID
     * @return A list of all commands newer than the given number
     */
    List<BaseCommand> getCommandsSinceLastCommand(String game_name, String player_name, int lastCommandID);

    /**
     * Makes sure that the input command is either number zero or one number higher than the previous command
     * @param command
     * @return whether the command is the correct number
     */
    boolean validateCommand(BaseCommand command);

    /**
     * Finds the number of DrawTrainCard commands since the last EndTurn command
     * @param game_name
     * @return the number of DrawTrainCard commands since the last EndTurn command
     */
    int getNumberOfDrawTrainCommands(String game_name);

    /*
    End game
     */

    /**
     * Checks if a full round has passed since the LastRound command was added.
     * @param game_name
     * @return whether or not the game is over
     */
    boolean isEndOfGame(String game_name);

    /**
     * Looks to see if the game contains a LastRound command
     * @param game_name
     * @return whether the game is already in the LastRound phase
     */
    boolean alreadyLastRound(String game_name);

    /*
    Getting the game model
     */

    /**
     * Builds and returns the current model of the game, as stored in the database
     * @param game_name
     * @return the constructed GameModel
     */
    GameModel getGameModel(String game_name);

    void updateCurrentPlayer(String game_name, int nextPlayer);
}
