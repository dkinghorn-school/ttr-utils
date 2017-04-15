package edu.goldenhammer.model;

//import edu.goldenhammer.database.postgresql.data_types.SQLTrainCard;

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

    //TODO: make this work
//    public static TrainCard parseDatabaseTrainCard(SQLTrainCard sqlTrainCard) {
//        Color color = Color.getTrainCardColorFromString(sqlTrainCard.getTrainType());
//        return new TrainCard(color);
//    }
}
