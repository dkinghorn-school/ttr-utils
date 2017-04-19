package edu.goldenhammer.server.commands;

import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.database.IGameDAO;
import edu.goldenhammer.model.DestinationCard;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seanjib on 3/4/2017.
 */
public class ReturnDestCardsCommand extends BaseCommand {
    List<DestinationCard> toReturn;

    public ReturnDestCardsCommand() {
        setName("ReturnDestCards");
    }

    //TODO: make this synchronized for part 1. make sure it's the right command number. if not, change it.
    public  Results execute() {
        IGameDAO dbc = DatabaseController.getGameDAO();
        Results results = new Results();
        try {
            if(toReturn.size() == 0 || dbc.returnDestCards(getGameName(), getPlayerName(), toReturn)) {
                //TODO: do checking to make sure this adds. If it doesn't make sure it reverses what it did.
//                dbc.addCommand(this, true, false);
                results.setResponseCode(200);
                Serializer serializer = new Serializer();
                results.setMessage(serializer.serialize(this));
                addToDatabase(true, false);
            }
            else {
                results.setResponseCode(400);
                results.setAndSerializeMessage("Error: You cannot return those cards");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            results.setResponseCode(400);
            results.setAndSerializeMessage("Error: Invalid input");
        }
        return results;
    }

    @Override
    public void hide(){
        toReturn = new ArrayList<>();
    }

    @Override
    public boolean endTurn() {
        IGameDAO dbc = DatabaseController.getGameDAO();
        return dbc.allHandsInitialized(getGameName());
    }
}
