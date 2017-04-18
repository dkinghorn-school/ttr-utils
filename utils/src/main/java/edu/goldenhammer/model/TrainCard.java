package edu.goldenhammer.model;

import java.io.Serializable;

/**
 * Created by seanjib on 3/2/2017.
 */
public class TrainCard implements Serializable{
    private Color color;

    public TrainCard(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}
