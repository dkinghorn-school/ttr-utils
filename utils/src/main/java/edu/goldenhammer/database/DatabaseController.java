package edu.goldenhammer.database;



/**
 * Created by devonkinghorn on 2/4/17.
 */
public class DatabaseController {

    private static IDatabaseController singleton;

    public static void setInstance(IDatabaseController controller) {
        singleton = controller;
    }
    /**
     * @pre an instance was set with setInstance
     *
     * @post an instance of IDatabaseController is returned
     * @return an instance of IDatabaseController
     */
    public static IDatabaseController getInstance(){

        return singleton;
    }
    //TODO: remove this method
    public static void setFirstInstance(int maxTrain) {

//        singleton = new SQLController(maxTrain);
    }

}
