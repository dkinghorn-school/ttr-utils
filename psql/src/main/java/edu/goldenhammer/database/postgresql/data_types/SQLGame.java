package edu.goldenhammer.database.postgresql.data_types;

import java.io.Serializable;

/**
 * Created by seanjib on 2/3/2017.
 */
public class SQLGame implements Serializable {

    public static final String ID = "game_id";
    public static final String GAME_NAME = "name";
    public static final String STARTED = "started";
    public static final String TABLE_NAME = "game";
    public static final String CREATE_STMT = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (\n" +
                    "    %2$s SERIAL UNIQUE,\n" +
                    "    %3$s VARCHAR(20) UNIQUE NOT NULL,\n" +
                    "    %4$s BOOLEAN NOT NULL,\n" +
                    "    PRIMARY KEY(%2$s)" +
                    ");"
            , TABLE_NAME, ID, GAME_NAME, STARTED);

    public SQLGame(String id, String name, Boolean started){
        this.id = id;
        this.name = name;
        this.started = started;
    }

    public String getID() {
        return id;
    }


    public String getName() {
        return name;
    }


    public Boolean isStarted() {
        return started;
    }

    private String id;
    private String name;
    private Boolean started;

}
