package edu.goldenhammer.model;

import java.util.List;

/**
 * Created by devonkinghorn on 2/8/17.
 */
public class GameListItem implements IGameListItem {
    public GameListItem(String id, String name, boolean started, List<String> players){
        this.id = id;
        this.name = name;
        this.started = started;
        this.players = players;
    }
    
    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isStarted() {
        return started;
    }

    public List<String> getPlayers() {
        return players;
    }

    @Override
    public void setPlayers(List<String> players) { this.players = players; }

    private String id;
    private String name;
    private boolean started;
    private List<String> players;

}
