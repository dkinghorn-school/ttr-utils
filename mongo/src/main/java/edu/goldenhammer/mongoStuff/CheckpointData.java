package edu.goldenhammer.mongoStuff;

import edu.goldenhammer.model.GameModel;
import edu.goldenhammer.model.Hand;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * Created by devonkinghorn on 4/10/17.
 */
public class CheckpointData implements Serializable {
    GameModel game;
    TrainCards trainCards;
    DestCards destCards;
    TreeMap<String, Hand> hands;

    public CheckpointData(GameModel game, TrainCards trainCards, DestCards destCards, TreeMap<String, Hand> hands) {
        this.game = game;
        this.trainCards = trainCards;
        this.destCards = destCards;
        this.hands = hands;
    }

    public GameModel getGame() {
        return game;
    }

    public void setGame(GameModel game) {
        this.game = game;
    }

    public TrainCards getTrainCards() {
        return trainCards;
    }

    public void setTrainCards(TrainCards trainCards) {
        this.trainCards = trainCards;
    }

    public DestCards getDestCards() {
        return destCards;
    }

    public void setDestCards(DestCards destCards) {
        this.destCards = destCards;
    }

    public TreeMap<String, Hand> getHands() {
        return hands;
    }

    public void setHands(TreeMap<String, Hand> hands) {
        this.hands = hands;
    }
}
