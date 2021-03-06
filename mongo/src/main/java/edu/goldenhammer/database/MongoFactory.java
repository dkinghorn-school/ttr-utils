package edu.goldenhammer.database;

/**
 * Created by McKean on 4/17/2017.
 */

public class MongoFactory implements AbstractFactory {
    @Override
    public IGameDAO getGameDAO() {
       return new MongoGameDAO();
    }

    @Override
    public IUserDAO getUserDAO() {
        return new MongoUserDAO();
    }
}
