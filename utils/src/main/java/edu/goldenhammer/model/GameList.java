package edu.goldenhammer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by seanjib on 2/3/2017.
 */
public class GameList implements Serializable {
    public GameList() {
        gameList = new ArrayList<>();
    }

    public List<GameListItem> getGameList() {
        return gameList;
    }

    public void add(GameListItem g) {
        gameList.add(g);
    }

    private List<GameListItem> gameList;

    public void filterOut(String player){
        Iterator<GameListItem> i = getGameList().iterator();
        while(i.hasNext()){
            GameListItem listItem = i.next();
            if(listItem.getPlayers().contains(player)){
                i.remove();
            }
        }
    }

    public void filterFull(){
        int max = 4;
        Iterator<GameListItem> i = getGameList().iterator();
        while(i.hasNext()){
            GameListItem listItem = i.next();
            if(listItem.getPlayers().size() > max){
                i.remove();
            }
        }

    }
}
