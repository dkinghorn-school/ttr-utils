package edu.goldenhammer.database;

/**
 * Created by McKean on 4/17/2017.
 */

public class SQLFactory implements AbstractFactory {
    @Override
    public IGameDAO getGameDAO() {
        return null;
    }

    @Override
    public IUserDAO getUserDAO() {
        return null;
    }
}
