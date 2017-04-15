package edu.goldenhammer.model;

/**
 * Created by seanjib on 3/2/2017.
 */
public class Bank {
    private AvailableTrainCards availableTrainCards;

    public Bank(TrainCard[] trainCards) {
        this.availableTrainCards = new AvailableTrainCards(trainCards);
    }
}
