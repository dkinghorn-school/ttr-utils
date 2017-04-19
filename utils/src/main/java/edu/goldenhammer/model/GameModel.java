package edu.goldenhammer.model;


import java.util.List;

/**
 * Created by seanjib on 2/11/2017.
 */
public class GameModel implements IGameModel {
    private List<PlayerOverview> players;
    private Map map;
    private GameName name;
    private List<Color> mBank;
    private java.util.Map<String,Hand> hands;
    private boolean lastRound;
    private int checkpointIndex;
    private int currentTurn;
    private String state;

    public GameModel(List<PlayerOverview> players, Map map, GameName name, List<Color> mBank) {
        this.players = players;
        this.map = map;
        this.name = name;
        this.mBank = mBank;
        checkpointIndex = -1;
        lastRound = false;
    }

    public void setHands(java.util.Map<String, Hand> hands){
        this.hands = hands;
    }

    public java.util.Map<String, Hand> getHands() {
        return hands;
    }

    public void setLastRound(boolean answer){
        lastRound = answer;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public int getCheckpointIndex() {
        return checkpointIndex;
    }

    public void setCheckpointIndex(int checkpointIndex) {
        this.checkpointIndex = checkpointIndex;
    }

    public List<PlayerOverview> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerOverview> players) {
        this.players = players;
    }

    public Map getMap() {
        return map;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public GameName getName() {
        return name;
    }

    public void setName(GameName name) {
        this.name = name;
    }

    public List<Color> getBank() {
        return mBank;
    }

    public void setBank(List<Color> mBank) {
        this.mBank = mBank;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}