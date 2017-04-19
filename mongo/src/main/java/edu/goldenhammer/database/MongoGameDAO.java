package edu.goldenhammer.database;

import edu.goldenhammer.model.*;
import edu.goldenhammer.model.Map;
import edu.goldenhammer.mongoStuff.MongoDriver;
import edu.goldenhammer.mongoStuff.MongoGame;
import edu.goldenhammer.server.commands.*;
import javafx.util.Pair;

import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
/**
 * Created by McKean on 4/17/2017.
 */

public class MongoGameDAO implements IGameDAO{
    private int MAX_TRAIN;
    private MongoDriver driver;

    private ConcurrentHashMap<String, City> allCities;

    public static final int ROUTE_COUNT = 101;
    public static final int CITY_COUNT = 35;
    public static final int MAX_DESTINATION_CARDS = 76;
    private int betweenCheckpoint;


    private ConcurrentHashMap<String,MongoGame> mongoGames;
    public MongoGameDAO(int maxTrain, int betweenCheckpoint) {
        MAX_TRAIN=maxTrain;
        driver = new MongoDriver();
        mongoGames = new ConcurrentHashMap<>();
        allCities = new ConcurrentHashMap<>();
        this.betweenCheckpoint = betweenCheckpoint;

    }

    public MongoGameDAO(){
        MAX_TRAIN=45;
        driver = new MongoDriver();
        mongoGames = new ConcurrentHashMap<>();

        allCities = new ConcurrentHashMap<>();

        betweenCheckpoint = 5;

    }

    private MongoGame getGame(String game_name) {
        synchronized (Lock.getInstance().getLock(game_name)){
            MongoGame game;
            if(mongoGames.containsKey(game_name)) {
                game = mongoGames.get(game_name);
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

    }


    private  int getPlayerNumber(MongoGame currentGame, String player_name) {
        int playerId = -1;
        for(PlayerOverview player : currentGame.getCheckpoint().getPlayers()) {
            if(player.getUsername().equals(player_name)) {
                playerId = player.getPlayer();
            }
        }
        return playerId;
    }

    @Override
    public void setCheckpointLength(int checkpointLength){
        betweenCheckpoint = checkpointLength;
    }

    @Override
    public void setMaxTrains(int i) {
        MAX_TRAIN = i;
    }

    @Override
    public  List<String> getPlayers(String gameID) {
        try{
            MongoGame mg = driver.getGame(gameID);
            if (mg == null){
                return null;
            }
            else{
                return mg.getPlayers();
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public  Boolean createGame(String name) {
        synchronized (Lock.getInstance().getLock(name)) {
            try {
                MongoGame g = driver.getGame(name);
                if (g == null) {
                    MongoGame creation = new MongoGame(name);
                    driver.setGame(creation);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public void clear(){

        try {
            driver.clearAll();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GameList getGames() {
        try{
            List<MongoGame> list = driver.getAllGames();
            GameList gameList = new GameList();
            for (MongoGame mg : list){
                Boolean started = mg.getCheckpoint() != null;
                if(!started) {
                    gameList.add(new GameListItem(mg.getGameName(), mg.getGameName(), started, mg.getPlayers()));
                }
            }
            return gameList;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
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
        synchronized (Lock.getInstance().getLock(gameName)) {
            try {
                MongoGame mg = driver.getGame(gameName);
                if (mg == null || mg.getPlayers().size() >= 5 || mg.getCheckpoint() != null) {
                    return false;
                } else {
                    List<String> players = mg.getPlayers();
                    if (!players.contains(player))
                        players.add(player);
                    mg.setPlayers(players);
                    driver.setGame(mg);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    @Override
    public Boolean leaveGame(String player, String gameName) {
        synchronized (Lock.getInstance().getLock(gameName)) {
            try {
                MongoGame mg = driver.getGame(gameName);
                if (mg == null || mg.getCheckpoint() != null) {
                    return false;
                } else {
                    List<String> players = mg.getPlayers();
                    if (!players.contains(player)) {
                        return false;
                    } else {
                        players.remove(player);
                        mg.setPlayers(players);
                        driver.setGame(mg);
                        return true;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    @Override
    public void maybeDropGame(String gameName) {
        try{
            MongoGame mg = driver.getGame(gameName);
            if (mg.getPlayers().isEmpty()){
                driver.removeGame(mg.getGameName());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public IGameModel playGame(String gameID) {
        synchronized (Lock.getInstance().getLock(gameID)) {
            try {
                MongoGame mg = getGame(gameID);
                if (mg == null) {
                    return null;
                } else if (mg.getCheckpoint() != null) {
                    return new MongoDriver().getGame(gameID).getCheckpoint();
                } else {
                    List<PlayerOverview> leaderboard = new ArrayList<>();

                    for (int i = 0; i < mg.getPlayers().size(); i++) {
                        leaderboard.add(new PlayerOverview(Color.getPlayerColorFromNumber(i), MAX_TRAIN, 0, i, mg.getPlayers().get(i), 0));
                    }
                    Map map = initializeMap();
                    List<TrainCard> trainCardDeck = initializeTrainCards();
                    List<DestinationCard> destCardDeck = initializeDestCards();
                    List<Color> bank = new ArrayList<>();

                    for (int i = 0; i < 5; i++) {
                        bank.add((trainCardDeck.get(0).getColor()));
                        trainCardDeck.remove(0);
                    }


                    GameName g = new GameName(gameID);
                    GameModel model = new GameModel(leaderboard, map, g, bank);

                    mg.setCheckpoint(model);
                    mg.setCheckpointIndex(-1); //TODO should this be -1 or 0
                    mg.setDestDeck(destCardDeck);
                    mg.setTrainDeck(trainCardDeck);
//                new MongoDriver().setGame(mg);
                    if (mg.getCommands().size() == 0)
                        initializeHands(mg);
                    return model;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    private void initializeHands(MongoGame game){

        for(int i = 0; i < game.getPlayers().size();i++) {
            game.getHands().put(game.getPlayers().get(i), new Hand());
            InitializeHandCommand newHand = new InitializeHandCommand();
            newHand.setGameName(game.getGameName());
            newHand.setPlayerName(game.getPlayers().get(i));
            newHand.setPlayerNumber(i);
            newHand.setCommandNumber(i);
            newHand.execute();
        }
    }

    private Map initializeMap(){
        List<City> cities = initializeCities();
        List<Track> tracks = initializeTracks();
        Map map = new Map(tracks,cities);
        return map;
    }

    private List<City> initializeCities(){
        List<City> cityList = new ArrayList<>();
        String pathName = "/cities.txt";
        Scanner cities = new Scanner(getClass().getResourceAsStream(pathName));
        for (int i = 0; i < CITY_COUNT * 3; i += 3) {
            String city = cities.nextLine();
            String[] vars = city.split(",");
            City c = new City(Integer.parseInt(vars[1]),Integer.parseInt(vars[2]),vars[0]);
            cityList.add(c);
            allCities.put(vars[0],c);
        }
        return cityList;
    }

    private List<Track> initializeTracks(){
        List<Track> trackList = new ArrayList<>();
        String pathName = "/routes.txt";
        Scanner routes = new Scanner(getClass().getResourceAsStream(pathName));
        TreeSet<String> connections = new TreeSet<>();
        for (int i = 0; i < ROUTE_COUNT * 5; i += 5) {
            String route = routes.nextLine();
            String[] vars = route.split(",");
            City city1 = allCities.get(vars[0]);
            City city2 = allCities.get(vars[1]);
            int length = Integer.parseInt(vars[3]);
            int id = (i / 5) + 1;
            Color color = Color.getTrackColorFromString(vars[2]);
            String destString = city1.getName() + city2.toString();
            Boolean secondTrack = connections.contains(destString);
            Track t = new Track(city1,city2,length,color,-1,
                    city1.getX_location(),city1.getY_location(),
                    city2.getX_location(),city2.getY_location(),
                    id,secondTrack);
            trackList.add(t);
            connections.add(destString);

        }
        return trackList;
    }

    private List<DestinationCard> initializeDestCards(){
        List<DestinationCard> destCardList = new ArrayList<>();
        String pathName = "/destinations.txt";
        Scanner destinations = new Scanner(getClass().getResourceAsStream(pathName));
        for(int i = 0; i < MAX_DESTINATION_CARDS * 4; i += 4) {
            String destination = destinations.nextLine();
            String[] vars = destination.split(",");
            City city1 = allCities.get(vars[0]);
            City city2 = allCities.get(vars[1]);
            int points = Integer.parseInt(vars[2]);
            DestinationCard d = new DestinationCard(city1,city2,points);
            destCardList.add(d);

        }
        return destCardList;
    }

    private List<TrainCard> initializeTrainCards(){
        List<TrainCard> trainCardList = new ArrayList<>();
        for (int i=0; i<12; i++){
            trainCardList.add(new TrainCard(Color.BLACK));
            trainCardList.add(new TrainCard(Color.BLUE));
            trainCardList.add(new TrainCard(Color.PURPLE));
            trainCardList.add(new TrainCard(Color.RED));
            trainCardList.add(new TrainCard(Color.GREEN));
            trainCardList.add(new TrainCard(Color.ORANGE));
            trainCardList.add(new TrainCard(Color.YELLOW));
            trainCardList.add(new TrainCard(Color.WHITE));
            trainCardList.add(new TrainCard(Color.WILD));
        }
        trainCardList.add(new TrainCard(Color.WILD));
        trainCardList.add(new TrainCard(Color.WILD));
        return trainCardList;
    }

    @Override
    public boolean allHandsInitialized(String gameName) {
        int initializedHands = 0;
        List<BaseCommand> commands = getGame(gameName).getCommands();
        for(BaseCommand cmd: commands){
            if(cmd instanceof ReturnDestCardsCommand){
                initializedHands++;
            }
        }
        if(initializedHands >= getGame(gameName).getPlayers().size()){
            return true;
        }
        return false;
    }

    @Override
    public TrainCard drawRandomTrainCard(String gameName, String playerName) {
        MongoGame mg = (MongoGame) mongoGames.get(gameName);
        if (mg != null) {
            java.util.Map<String, Hand> hands = mg.getHands();
            Hand playerHand = hands.get(playerName);

            TrainCard card = getTopTrainCard(mg);
            if(card == null) {
                return null;
            }
            playerHand.addTrainCard(card);

            hands.put(playerName, playerHand);
            mg.setHands(hands);
            return card;
        }
        return null;
    }

    @Override
    public TrainCard drawTrainCardFromSlot(String game_name, String player_name, int slot) {
        MongoGame mg = (MongoGame)mongoGames.get(game_name);
        if(mg != null) {
            GameModel checkpoint = mg.getCheckpoint();
            List<Color> bank = checkpoint.getBank();

            java.util.Map<String, Hand> hands = mg.getHands();
            Hand hand = hands.get(player_name);

            TrainCard replacementCard = getTopTrainCard(mg);

            Color selected = bank.get(slot);
            if(selected == null || replacementCard == null) {
                return null;
            }

            TrainCard selectedCard = new TrainCard(selected);

            hand.addTrainCard(selectedCard);
            bank.set(slot, replacementCard.getColor());

            hands.put(player_name, hand);
            mg.setHands(hands);
            checkpoint.setBank(bank);
//            mg.setCheckpoint(checkpoint);
            return new TrainCard(selected);
        }
        return null;
    }


    @Override
    public boolean hasDrawnTwoTrainCards(String game_name, String player_name) {
        try {
            MongoGame mg = driver.getGame(game_name);
            List<BaseCommand> commands = mg.getCommands();
            int lastEndTurnIndex = getLastEndTurnCommandIndex(game_name);
            BaseCommand lastCommand = commands.get(lastEndTurnIndex);
            if(lastCommand instanceof LastTurnCommand) {
                if(!lastCommand.getPlayerName().equals(player_name)) {
                    return true;
                }
            }

            int drawTrainCommandCount = 0;
            for(int i = lastEndTurnIndex; i < commands.size(); i++) {
                if(commands.get(i) instanceof DrawTrainCardCommand) {
                    ++drawTrainCommandCount;
                }
            }
            if(drawTrainCommandCount == 1){
                mg.getCheckpoint().setState("2ndtrain");
            }
            return drawTrainCommandCount >= 2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private int getLastEndTurnCommandIndex(String game_name) {
        try {
            MongoGame mg = driver.getGame(game_name);
            List<BaseCommand> commands = mg.getCommands();
            for(int i = commands.size() - 1; i >= 0; i--) {
                if(commands.get(i) instanceof EndTurnCommand) {
                    return i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    @Override
    public void redealSlotCards(String game_name) {
        MongoGame mg = (MongoGame)mongoGames.get(game_name);
        if(mg != null) {
            GameModel checkpoint = mg.getCheckpoint();
            List<Color> bank = checkpoint.getBank();
            List<TrainCard> discardedCards = mg.getTrainDiscard();

            for(int i = 0; i < bank.size(); i++) {
                Color discarded = bank.get(i);
                TrainCard newDiscardedCard = new TrainCard(discarded);
                TrainCard newBankCard = getTopTrainCard(mg);
                if(newBankCard == null) {
                    mg.setTrainDiscard(discardedCards);
                    checkpoint.setBank(bank);
                    mg.setCheckpoint(checkpoint);
                    return;
                }
                discardedCards.add(newDiscardedCard);
                bank.set(i, newBankCard.getColor());
            }
            mg.setTrainDiscard(discardedCards);
            checkpoint.setBank(bank);
            mg.setCheckpoint(checkpoint);
        }
    }

    @Override
    public boolean discardCard(String gameName, String playerName, Color color) {
        MongoGame mg = (MongoGame)mongoGames.get(gameName);
        if(mg != null) {
            java.util.Map<String, Hand> hands = mg.getHands();
            Hand hand = hands.get(playerName);
            boolean success = hand.removeTrainCard(color);

            hands.put(playerName, hand);
            mg.setHands(hands);
            return success;
        }
        return false;
    }

    @Override
    public List<Color> getSlotCardColors(String game_name) {
        MongoGame mg = (MongoGame)mongoGames.get(game_name);
        if(mg != null) {
            GameModel checkpoint = mg.getCheckpoint();
            return checkpoint.getBank();
        }
        return null;
    }

    @Override
    public DestinationCard drawRandomDestinationCard(String gameName, String playerName) {
        // Need to pull it out of the cards and put it into limbo...
        MongoGame currentGame = getGame(gameName);
        Random rand = new Random();
        currentGame.getCheckpoint().setState("returnDestCards");
        int n = rand.nextInt(currentGame.getDestDeck().size());
        DestinationCard randomCard = currentGame.getDestDeck().get(n);
        currentGame.getDestDeck().remove(n);
        java.util.Map<String, Hand> hands = currentGame.getHands();
        hands.get(playerName).getDrawnDestinationCards().addCard(randomCard);
        return randomCard;
    }

    @Override
    public List<DestinationCard> getPlayerDestinationCards(String game_name, String player_name) {
        MongoGame currentGame = getGame(game_name);
        Hand playerHand = currentGame.getHands().get(player_name);
        return playerHand.getDestinationCards();
    }


    @Override
    public boolean returnDestCards(String gameName, String playerName, List<DestinationCard> destinationCards) {
        MongoGame currentGame = getGame(gameName);
        Hand playerHand = currentGame.getHands().get(playerName);

        //If needs to test if it's initializing hand, then it has to be 0 or 1 card, otherwise can be up to 2.
        if((currentGame.getCommands().get(currentGame.getCommands().size()-1)).getName().equals("InitializeHand")) {
            if(destinationCards.size() > 1) {
                return false;
            }
        } else {
            if(destinationCards.size() > 2) {
                return false;
            }
        }

        //List of drawn but not added dest cards
        List<DestinationCard> playerCards = asList(playerHand.getDrawnDestinationCards().getCards());

        //Go through the discarded cards and make sure they are in the players hand
        for(DestinationCard disCard : destinationCards)
        {
            if(!playerCards.contains(disCard))
                return false;
        }
        for(DestinationCard disCard : destinationCards)
        {
            currentGame.getDestDiscard().add(disCard);
        }
        for(DestinationCard desCard: playerCards) {
            if(!destinationCards.contains(desCard))
                playerHand.getDestinationCards().add(desCard);
        }

        playerHand.setDrawnDestinationCards(new DrawnDestinationCards(new ArrayList<DestinationCard>()));
        return true;
    }

    @Override
    public boolean postMessage(String game_name, String player_name, String message) {
        MongoGame game = getGame(game_name);
        game.getChatMessages().add(new Message(player_name,message));
        try {
            MongoGame oldgame = driver.getGame(game_name);
            oldgame.getChatMessages().add(new Message(player_name, message));
            driver.setGame(oldgame);
        }catch (UnknownHostException e){
            return false;
        }
        return true;
    }

    @Override
    public List<Message> getMessages(String game_name) {
        MongoGame game = getGame(game_name);
        return game.getChatMessages();
    }

    @Override
    public boolean canClaimRoute(String game_name, String username, int route_number) {
        MongoGame currentGame = getGame(game_name);
        int playerId = getPlayerNumber(currentGame, username);
        int trainsLeft = -1;
        boolean result = false;

        City city1 = null;
        City city2 = null;

        for (PlayerOverview player : currentGame.getCheckpoint().getPlayers()) {
            if (player.getUsername().equals(username)) {
                trainsLeft = player.getPieces();
            }
        }

        for(Track track : currentGame.getCheckpoint().getMap().getTracks()) {
            if(track.getRoute_number() == route_number) {
                city1 = track.getCity1();
                city2 = track.getCity2();
                if(track.getOwner() == -1) {
                    if(trainsLeft > track.getLength()) {
                        result = true;
                    }
                }
            }
        }

        //Check if double routes are allowed...
        if(currentGame.getPlayers().size() > 3) {
            for(Track track : currentGame.getCheckpoint().getMap().getTracks()) {
                if(track.getRoute_number() != route_number && track.getCity1().equals(city1) && track.getCity2().equals(city2)) {
                    // If the person owns the other route return false
                    if(track.getOwner() == playerId) {
                        result = false;
                    }
                }
            }
        }

        return result;
    }


    @Override
    public boolean claimRoute(String game_name, String username, int route_number) {
        boolean result = false;
        MongoGame currentGame = getGame(game_name);
        for(Track track : currentGame.getCheckpoint().getMap().getTracks()) {
            if(track.getRoute_number() == route_number) {
                track.setOwner(getPlayerNumber(currentGame, username));
                result = true;
            }
        }
        return result;
    }

    @Override
    public void removeTrainsFromPlayer(String game_name, String username, int trainsToRemove) {
        MongoGame currentGame = getGame(game_name);

        for(PlayerOverview player : currentGame.getCheckpoint().getPlayers()) {
            if(player.getUsername().equals(username)) {
                player.setPieces(player.getPieces() - trainsToRemove);
            }
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
        try {
            MongoGame game = getGame(cmd.getGameName());
            game.getCommands().add(cmd);
            game.getCheckpoint().setCheckpointIndex(cmd.getCommandNumber());
            if (!(game.getCommands().size() - (game.getCheckpointIndex() + 1) == betweenCheckpoint || !allHandsInitialized(cmd.getGameName()))) {
                MongoGame oldgame = driver.getGame(cmd.getGameName());
                oldgame.getCommands().add(cmd);
                driver.setGame(oldgame);
            }else{
                game.setCheckpointIndex(game.getCheckpointIndex()+betweenCheckpoint);
                driver.setGame(game);
            }
            return true;
        }catch (UnknownHostException e){   }
        return false;
    }

    @Override
    public EndTurnCommand getEndTurnCommand(String gameName, int commandNumber, String playerName) {
        EndTurnCommand newEndTurn = new EndTurnCommand();
        MongoGame currentGame = getGame(gameName);
        int numPlayers = currentGame.getCheckpoint().getPlayers().size();
        int playerId = getPlayerNumber(currentGame, playerName);

        currentGame.getCheckpoint().setState("newTurn");
        newEndTurn.setPreviousPlayer(playerId);
        newEndTurn.setGameName(gameName);
        newEndTurn.setCommandNumber(commandNumber);
        newEndTurn.setPlayerName(playerName);
        if(playerId < (numPlayers-1)) {
            newEndTurn.setNextPlayer(playerId+1);
        } else {
            newEndTurn.setNextPlayer(0);
        }

        return newEndTurn;
    }
    @Override
    public List<BaseCommand> getCommandsSinceLastCommand(String game_name, String player_name, int lastCommandID) {
        List<BaseCommand> remainingCommands = new ArrayList<BaseCommand>();
        MongoGame currentGame = getGame(game_name);
        for(BaseCommand command : currentGame.getCommands()) {
            if(command.getCommandNumber() > lastCommandID) {
                remainingCommands.add(command);
            }
        }
        return remainingCommands;
    }

    @Override
    public boolean validateCommand(BaseCommand command) {
        boolean valid = false;
        MongoGame currentGame = getGame(command.getGameName());

        int commandNumber = command.getCommandNumber();
        int lastCommandExecuted = currentGame.getCommands().size() - 1;
        if(commandNumber == (lastCommandExecuted+1)) {
            valid = true;
        }

        return valid;
    }

    @Override
    public int getNumberOfDrawTrainCommands(String game_name) {
        MongoGame currentGame = getGame(game_name);
        int numberOfDrawCommands = 0;
        int indexOfLastEndTurn = 0;

        for(BaseCommand command : currentGame.getCommands()) {
            if(command.getName().equals("EndTurn")) {
                indexOfLastEndTurn = command.getCommandNumber();
            }
        }

        for(int i=indexOfLastEndTurn; i<currentGame.getCommands().size(); i++) {
            if(currentGame.getCommands().get(i).getName().equals("DrawTrainCard")) {
                numberOfDrawCommands++;
            }
        }

        return numberOfDrawCommands;
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

    @Override
    public void updateCurrentPlayer(String game_name, int nextPlayer) {
        getGameModel(game_name).setCurrentTurn(nextPlayer);

    }

    private boolean shuffleTrainCards(MongoGame mg) {
        List<TrainCard> deck = mg.getTrainDeck();
        List<TrainCard> discard = mg.getTrainDiscard();

        for(TrainCard card : discard) {
            deck.add(card);
        }

        Collections.shuffle(deck, new Random(System.nanoTime()));
        mg.setTrainDeck(deck);
        mg.setTrainDiscard(discard);
        return deck.size() > 0;
    }

    private TrainCard getTopTrainCard(MongoGame mg) {
        List<TrainCard> deck = mg.getTrainDeck();
        if (deck.size() == 0) {
            if (!shuffleTrainCards(mg)) {
                return null;
            }
        }
        TrainCard card = deck.remove(0);
        mg.setTrainDeck(deck);
        return card;
    }
}

