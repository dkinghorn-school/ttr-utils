package edu.goldenhammer.database;

//import edu.goldenhammer.database.postgresql.SQLController;

/**
 * Created by devonkinghorn on 2/4/17.
 */
public class DatabaseController {

    private static IDatabaseController singleton;
    private static IGameDAO gameDAO;
    private static IUserDAO userDAO;


    public static IGameDAO getGameDAO(){
        return gameDAO;
    }

    public static IUserDAO getUserDAO(){
        return userDAO;
    }

    public static void setUserDAO(IUserDAO dao){
        userDAO = dao;
    }

    public static void setGameDAO(IGameDAO dao){
        gameDAO = dao;
    }

    /**
     * @pre The PSQL database is able to connect with the credentials in the config
     *
     * @post an instance of DatabaseController is returned with a connection to the SQL database
     */
    public static IDatabaseController getInstance(){
//        if(singleton == null)
//            singleton = new SQLController();
        return singleton;
    }

    public static void setFirstInstance(int maxTrain) {
//        singleton = new SQLController(maxTrain);
    }


}
