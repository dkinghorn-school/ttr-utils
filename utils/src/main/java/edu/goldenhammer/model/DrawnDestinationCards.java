package edu.goldenhammer.model;

import java.util.List;

/**
 * Created by seanjib on 3/2/2017.
 */
public class DrawnDestinationCards {
    private DestinationCard[] cards;

    public DrawnDestinationCards(List<DestinationCard> cards) {
        this.cards = new DestinationCard[cards.size()];
        for(int i = 0; i < cards.size(); i++) {
            this.cards[i] = cards.get(i);
        }
    }
}
