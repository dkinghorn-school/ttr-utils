package edu.goldenhammer.database.postgresql.data_types;

import com.google.gson.Gson;
import edu.goldenhammer.model.GameModel;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by devonkinghorn on 4/5/17.
 */
public class SQLInitialGameModel implements Serializable {

    public static final String ID = "uid";
    public static final String GAME_NAME = "game_name";
    public static final String TABLE_NAME = "initial_game";
    public static final String INITIAL_STATE = "initial_state";
    public static final String CREATE_STMT = String.format(
            "CREATE TABLE IF NOT EXISTS %1$s (\n" +
                    "    %2$s SERIAL UNIQUE,\n" +
                    "    %4$s text,\n" +
                    "    %3$s VARCHAR(20) UNIQUE NOT NULL,\n" +
                    "    PRIMARY KEY(%2$s)" +
                    ");"
            , TABLE_NAME, ID, GAME_NAME, INITIAL_STATE);


    public static GameModel buildFromResultsSet(ResultSet resultSet) throws SQLException {
        Gson gson = new Gson();
        if(resultSet.next())
            return gson.fromJson(resultSet.getString(INITIAL_STATE),GameModel.class);
        return null;
    }
}
