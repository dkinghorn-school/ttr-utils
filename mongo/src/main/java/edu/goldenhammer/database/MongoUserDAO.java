package edu.goldenhammer.database;

import edu.goldenhammer.model.City;
import edu.goldenhammer.model.GameModel;
import edu.goldenhammer.model.Player;
import edu.goldenhammer.model.Track;
import edu.goldenhammer.mongoStuff.MongoDriver;
import edu.goldenhammer.mongoStuff.MongoGame;
import edu.goldenhammer.mongoStuff.MongoUser;
import javafx.util.Pair;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by McKean on 4/17/2017.
 */

public class MongoUserDAO implements IUserDAO{
    private MongoDriver driver;

    public MongoUserDAO(){
        driver = new MongoDriver();
    }

    @Override
    public void clear() {

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
}
