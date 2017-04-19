package edu.goldenhammer.server.commands;

import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.database.IGameDAO;
import edu.goldenhammer.model.Color;
import edu.goldenhammer.model.TrainCard;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

import java.util.List;

/**
 * Created by seanjib on 3/1/2017.
 */
public class DrawTrainCardCommand extends BaseCommand {
    private TrainCard card;
    private int slot;
    private Color drawnCard;
    private List<Color> bank;

    public Results execute() {
        IGameDAO dbc = DatabaseController.getGameDAO();
        Results results = new Results();
        results.setResponseCode(200);
        if(slot >= 0 && slot <= 4) {
            card = dbc.drawTrainCardFromSlot(getGameName(), getPlayerName(), slot);
            drawnCard = card.getColor();
            bank = getSlotCards(getGameName());
            while(hasThreeWilds(bank)) {
                dbc.redealSlotCards(getGameName());
                bank = getSlotCards(getGameName());
            }
//            dbc.addCommand(this, true, false);
            results.setMessage(Serializer.serialize(this));
        } else if(slot == 5) {
            card = dbc.drawRandomTrainCard(getGameName(), getPlayerName());
            drawnCard = card.getColor();
            bank = getSlotCards(getGameName());
//            dbc.addCommand(this, true, false);
            results.setMessage(Serializer.serialize(this));
        } else {
            results.setResponseCode(400);
            results.setAndSerializeMessage("Error: an error occurred while drawing a card from slot " + slot);
        }
        drawnCard = card.getColor();
        addToDatabase(true, false);
        return null;
    }

    @Override
    public boolean endTurn() {
        if(slot >= 0 && slot <= 4 && drawnCard == Color.WILD) {
            return true;
        }
        else if(hasDrawnTwo()) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean hasDrawnTwo() {
        IGameDAO dbc = DatabaseController.getGameDAO();
        return dbc.hasDrawnTwoTrainCards(getGameName(), getPlayerName());
    }

    private boolean drawingWildCardOnSecondDraw() {
        IGameDAO dbc = DatabaseController.getGameDAO();
        if (slot == 5){
            return false;
        }
        else if(dbc.getNumberOfDrawTrainCommands(getGameName()) == 0) {
            return false;
        }
        else {
            List<Color> slots = dbc.getSlotCardColors(getGameName());
            return slots.get(slot) == Color.WILD;
        }
    }

    private List<Color> getSlotCards(String game_name) {
        IGameDAO dbc = DatabaseController.getGameDAO();
        return dbc.getSlotCardColors(game_name);
    }

    @Override
    public boolean validate() {
        return super.validate() && !drawingWildCardOnSecondDraw();

    }

    private boolean hasThreeWilds(List<Color> bank) {
        int wildCount = 0;
        for(Color trainCard : bank) {
            if(trainCard == Color.WILD) {
                ++wildCount;
            }
        }
        return wildCount >= 3;
    }
}
