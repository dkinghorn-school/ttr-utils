package edu.goldenhammer.server.commands;

import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;

import edu.goldenhammer.database.IGameDAO;
import edu.goldenhammer.model.DestinationCard;
import edu.goldenhammer.model.DrawnDestinationCards;
import edu.goldenhammer.model.Hand;
import edu.goldenhammer.model.TrainCard;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seanjib on 3/4/2017.
 */
public class InitializeHandCommand extends BaseCommand {

    private Hand hand;

    public InitializeHandCommand() {
        setName("InitializeHand");
    }

    public Results execute() {
        IGameDAO dbc = DatabaseController.getGameDAO();

        Results results = new Results();
        hand = null;
        try {
            List<DestinationCard> playerDestinationCards = dbc.getPlayerDestinationCards(getGameName(), getPlayerName());
            if(playerDestinationCards.size() == 0) {
                hand = new Hand(new ArrayList<>(), drawTrainCards(dbc), drawDestinationCards(dbc));
                results.setResponseCode(200);
                Serializer serializer = new Serializer();
                results.setMessage(serializer.serialize(this));
                addToDatabase(true, false);
            }
            else {
                results.setResponseCode(400);
                results.setAndSerializeMessage("Error: You have already drawn your initial cards!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            results.setResponseCode(400);
            results.setAndSerializeMessage("Error: Invalid input!");
        }
        return results;
    }
    public void hide(){
        hand = new Hand();
    }

    private List<TrainCard> drawTrainCards(IGameDAO dbc) {
        List<TrainCard> trainCards = new ArrayList<>();
        //todo: see if we can assume this is 5
        for(int i = 0; i < 5; i++) {
            TrainCard trainCard = dbc.drawRandomTrainCard(getGameName(), getPlayerName());
            trainCards.add(trainCard);
        }

        return trainCards;
    }

    private DrawnDestinationCards drawDestinationCards(IGameDAO dbc) {
        List<DestinationCard> cards = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            DestinationCard destinationCard = dbc.drawRandomDestinationCard(getGameName(), getPlayerName());
            cards.add(destinationCard);
        }
        return new DrawnDestinationCards(cards);
    }

    @Override
    public boolean endTurn() {
        return false;
    }
}
