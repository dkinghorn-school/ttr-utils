package edu.goldenhammer.database.mongo;

import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.model.*;
import edu.goldenhammer.mongoStuff.MongoDriver;
import edu.goldenhammer.mongoStuff.MongoGame;
import edu.goldenhammer.mongoStuff.MongoUser;
import edu.goldenhammer.server.commands.BaseCommand;
import edu.goldenhammer.server.commands.EndTurnCommand;

import java.net.UnknownHostException;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by seanjib on 4/9/2017.
 */
public class MongoController implements IDatabaseController {
    private int MAX_TRAIN;
    private MongoDriver driver;

    private TreeMap mongoGames;

    public MongoController(int maxTrain) {
        MAX_TRAIN=maxTrain;
        driver = new MongoDriver();
        mongoGames = new TreeMap<String, GameModel>();
    }

    public MongoController(){
        MAX_TRAIN=45;
        driver = new MongoDriver();
        mongoGames = new TreeMap<String, GameModel>();
    }

    private MongoGame getGame(String game_name) {
        MongoGame game;
        if(mongoGames.containsKey(game_name)) {
            game = (MongoGame) mongoGames.get(game_name);
        } else {
            try {
                game = driver.getGame(game_name);
                if(game != null) {
                    mongoGames.put(game_name, game);
                }
            } catch (UnknownHostException uh) {
                uh.printStackTrace();
                game = null;
            }
        }
        return game;
    }

    private int getPlayerNumber(MongoGame currentGame, String player_name) {
        int playerId = -1;
        for(PlayerOverview player : currentGame.getCheckpoint().getPlayers()) {
            if(player.getUsername().equals(player_name)) {
                playerId = player.getPlayer();
            }
        }
        return playerId;
    }

    @Override
    public Player getPlayerInfo(String player) {
        try{
            MongoUser user = driver.getUser(player);
            if (user == null){
                return null;
            }
            else{
                return new Player(player,user.getToken());
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Boolean login(String username, String password) {
        MongoUser user;
        try{
            user = driver.getUser(username);
            return user!=null && user.getPassword().equals(password);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getPlayers(String gameID) {
        return null;
    }

    @Override
    public Boolean createUser(String username, String password) {
        MongoUser u = new MongoUser(username,password);
        try {
            if (driver.getUser(username) != null){
                return false;
            }
            else{
                driver.setUser(u);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void setAccessToken(String username, String accessToken) {
        try{
            MongoUser u = driver.getUser(username);
            u.setToken(accessToken);
            driver.setUser(u);
        }catch(Exception e){

        }
    }

    @Override
    public Boolean createGame(String name) {
        try{
            MongoGame g = driver.getGame(name);
            if (g == null){
                MongoGame creation = new MongoGame(name);
                driver.setGame(creation);
                return true;
            }
            else{
                return false;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public GameList getGames() {
        return null;
    }

    @Override
    public GameList getGames(String player) {
       try{
           List<MongoGame> games = driver.getGamesWithPlayer(player);
           GameList gameList = new GameList();
           for (MongoGame mg : games){
               Boolean started = mg.getCheckpoint() == null;
               GameListItem gli = new GameListItem(mg.getGameName(),mg.getGameName(),started,mg.getPlayers());
               gameList.add(gli);
           }
           return gameList;
       }catch(Exception e){
           e.printStackTrace();
           return null;
       }
    }

    @Override
    public Boolean joinGame(String player, String gameName) {
        try{
            MongoGame mg = driver.getGame(gameName);
            if (mg == null || mg.getPlayers().size() >= 5 || mg.getCheckpoint() != null){
                return false;
            }
            else{
                List<String> players = mg.getPlayers();
                players.add(player);
                mg.setPlayers(players);
                driver.setGame(mg);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean leaveGame(String player, String gameName) {
        try{
            MongoGame mg = driver.getGame(gameName);
            if (mg == null || mg.getCheckpoint() != null){
                return false;
            }
            else{
                List<String> players = mg.getPlayers();
                if (!players.contains(player)){
                    return false;
                }
                else{
                    players.remove(player);
                    mg.setPlayers(players);
                    driver.setGame(mg);
                    return true;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void maybeDropGame(String gameName) {
        try{
            MongoGame mg = driver.getGame(gameName);
            if (mg.getPlayers().isEmpty()){
                //driver needs delete function.
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }

    @Override
    public IGameModel playGame(String gameID) {
        return null;
    }

    @Override
    public boolean allHandsInitialized(String gameName) {
        return false;
    }

    @Override
    public TrainCard drawRandomTrainCard(String gameName, String playerName) {
        return null;
    }

    @Override
    public TrainCard drawTrainCardFromSlot(String game_name, String player_name, int slot) {
        return null;
    }

    @Override
    public boolean hasDrawnTwoTrainCards(String game_name, String player_name) {
        return false;
    }

    @Override
    public void redealSlotCards(String game_name) {

    }

    @Override
    public boolean discardCard(String gameName, String playerName, Color color) {
        return false;
    }

    @Override
    public List<Color> getSlotCardColors(String game_name) {
        return null;
    }

    @Override
    public DestinationCard drawRandomDestinationCard(String gameName, String playerName) {
        return null;
    }

    @Override
    public List<DestinationCard> getPlayerDestinationCards(String game_name, String player_name) {
        return null;
    }

    @Override
    public boolean returnDestCards(String gameName, String playerName, List<DestinationCard> destinationCards) {
        return false;
    }

    @Override
    public boolean postMessage(String game_name, String player_name, String message) {
        return false;
    }

    @Override
    public List<Message> getMessages(String game_name) {
        return null;
    }

    @Override
    public boolean canClaimRoute(String game_name, String username, int route_number) {
        return false;
    }

    @Override
    public boolean claimRoute(String game_name, String username, int route_number) {
        return false;
    }

    @Override
    public void removeTrainsFromPlayer(String game_name, String username, int trainsToRemove) {
        MongoGame currentGame = getGame(game_name);
        int playerId = this.getPlayerNumber(currentGame, username);

        for(PlayerOverview player : currentGame.getCheckpoint().getPlayers()) {
            player.setPieces(player.getPieces()-trainsToRemove);
        }
    }

    @Override
    public List<Track> getTracks(String game_name) {
        return this.getGame(game_name).getCheckpoint().getMap().getTracks();
    }

    @Override
    public int numTrainsLeft(String game_name, String player_name) {
        MongoGame currentGame = getGame(game_name);
        int playerTrains=MAX_TRAIN;
        int playerNumber = this.getPlayerNumber(currentGame, player_name);

        for(Track track : currentGame.getCheckpoint().getMap().getTracks()) {
            if(track.getOwner() == playerNumber) {
                playerTrains = playerTrains-track.getLength();
            }
        }

        return playerTrains;
    }

    @Override
    public boolean addCommand(BaseCommand cmd, boolean visibleToSelf, boolean visibleToAll) {
        return false;
    }

    @Override
    public EndTurnCommand getEndTurnCommand(String gameName, int commandNumber, String playerName) {
        return null;
    }

    @Override
    public List<BaseCommand> getCommandsSinceLastCommand(String game_name, String player_name, int lastCommandID) {
        return null;
    }

    @Override
    public boolean validateCommand(BaseCommand command) {
        return false;
    }

    @Override
    public int getNumberOfDrawTrainCommands(String game_name) {
        return 0;
    }

    @Override
    public boolean isEndOfGame(String game_name) {
        int player = -1;
        boolean lastRound = false;

        MongoGame currentGame = this.getGame(game_name);
        for (BaseCommand command : currentGame.getCommands()) {
            if (lastRound && (command.getName().equals("EndTurn") && command.getPlayerNumber() == player)) {
                return true;
            } else if (command.getName().equals("LastTurn")) {
                player = command.getPlayerNumber();
                lastRound = true;
            }
        }

        return false;
    }

    @Override
    public boolean alreadyLastRound(String game_name) {
        MongoGame currentGame = this.getGame(game_name);
        for (BaseCommand command : currentGame.getCommands()) {
            if (command.getName().equals("LastTurn")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GameModel getGameModel(String game_name) {
        return ((MongoGame)mongoGames.get(game_name)).getCheckpoint();
    }
}
