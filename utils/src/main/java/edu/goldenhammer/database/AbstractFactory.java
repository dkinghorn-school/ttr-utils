package edu.goldenhammer.database;

/**
 * Created by McKean on 4/17/2017.
 */

public interface AbstractFactory {

    public IGameDAO getGameDAO();

    public IUserDAO getUserDAO();
}
