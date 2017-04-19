package edu.goldenhammer.server.commands;

import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.database.IGameDAO;
import edu.goldenhammer.server.Results;

/**
 * Created by devonkinghorn on 3/29/17.
 */
public class LastTurnCommand extends BaseCommand {
    public LastTurnCommand() {
        setName("LastTurn");
    }

    @Override
    public Results execute() {
        IGameDAO dbc = DatabaseController.getGameDAO();
//        dbc.addCommand(this,true,true);
        addToDatabase(true, true);
        return new Results();
    }
}
