package edu.goldenhammer.mongoStuff;

import edu.goldenhammer.model.TrainCard;

import java.io.Serializable;
import java.util.List;

/**
 * Created by devonkinghorn on 4/10/17.
 */
public class TrainCards implements Serializable{
    List<TrainCard> deck;
    List<TrainCard> discarded;
}
