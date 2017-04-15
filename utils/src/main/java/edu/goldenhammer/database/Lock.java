package edu.goldenhammer.database;

import java.util.TreeMap;

/**
 * Created by devonkinghorn on 3/15/17.
 */
public class Lock {
    private static Lock singleton;

    public synchronized static Lock getInstance(){
        if (singleton == null) {
            singleton = new Lock();
        }
        return singleton;
    }
    private TreeMap<String, Object> locks;

    private Lock(){
        locks = new TreeMap<>();
    }

    public synchronized Object getLock(String gameName) {
        locks.putIfAbsent(gameName, new Object());
        return locks.get(gameName);
    }
}
