package edu.goldenhammer.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seanjib on 3/2/2017.
 */
public class Hand implements Serializable{
    private List<DestinationCard> destinationCards;
    private List<TrainCard> trainCards;
    private DrawnDestinationCards drawnDestinationCards;

    public Hand(List<DestinationCard> destinationCards, List<TrainCard> trainCards, DrawnDestinationCards drawnDestinationCards) {
        this.destinationCards = destinationCards;
        this.trainCards = trainCards;
        this.drawnDestinationCards = drawnDestinationCards;
    }

    public void addTrainCard(TrainCard card) {
        trainCards.add(card);
    }

    public void removeTrainCard(int index) {
        trainCards.remove(index);
    }

    public boolean removeTrainCard(Color color) {
        for(int i = 0; i < trainCards.size(); i++) {
            TrainCard current = trainCards.get(i);
            if(current.getColor() == color){
                trainCards.remove(i);
                return true;
            }
        }
        return false;
    }

    public int getTrainCardCount() {
        return trainCards.size();
    }

    public Hand(){
        this.destinationCards = new ArrayList<>();
        this.trainCards = new ArrayList<>();
        this.drawnDestinationCards = new DrawnDestinationCards(new ArrayList<>());
    }

    public List<DestinationCard> getDestinationCards() {
        return destinationCards;
    }

    public void setDestinationCards(List<DestinationCard> destinationCards) {
        this.destinationCards = destinationCards;
    }

    public List<TrainCard> getTrainCards() {
        return trainCards;
    }

    public void setTrainCards(List<TrainCard> trainCards) {
        this.trainCards = trainCards;
    }

    public DrawnDestinationCards getDrawnDestinationCards() {
        return drawnDestinationCards;
    }

    public void setDrawnDestinationCards(DrawnDestinationCards drawnDestinationCards) {
        this.drawnDestinationCards = drawnDestinationCards;
    }
}
