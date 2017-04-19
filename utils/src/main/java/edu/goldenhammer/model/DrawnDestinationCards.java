package edu.goldenhammer.model;

import java.util.List;

/**
 * Created by seanjib on 3/2/2017.
 */
public class DrawnDestinationCards {
    private List<DestinationCard> cards;

    public DrawnDestinationCards(List<DestinationCard> cards) {
        this.cards = cards;
    }

    public DestinationCard[] getCards() {
        return cards.toArray(new DestinationCard[cards.size()]);
    }

    public void addCard(DestinationCard card) {
        cards.add(card);
    }
}
