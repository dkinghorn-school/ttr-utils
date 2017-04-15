package edu.goldenhammer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;

/**
 * Created by jon on 3/24/17.
 */

public class ResultsGenerator {
    private List<EndResult> endResults;

    public ResultsGenerator() {
        endResults = new ArrayList<>();
    }

    public ResultsGenerator(String game_name) {
        endResults = new ArrayList<>();
        generateResults(game_name);
    }

    public List<EndResult> generateResults(String game_name) {
        IDatabaseController dbc = DatabaseController.getInstance();
        //Use the Database to grab the right game
        GameModel endModel = dbc.getGameModel(game_name);
        //From the GameModel get the Player Data
        List<PlayerOverview> players = endModel.getPlayers();
        //From the GameModel get the Map Data
        Map endMap = endModel.getMap();

        //All the Player Final Results
        List<EndResult> results = new ArrayList<>();

        for(PlayerOverview player : players) {
            int playerId = 0;
            int builtTrainPoints = 0;
            int completedDestinations = 0;
            int incompleteDestinations = 0; //Allows a negative number
            int longestContinuousTrain = 0;
            int total = 0;

            //set the right playerId
            playerId = player.getPlayer();

            //built Train points should be the same as the LeaderBoard Points...
            //This seems to be zero in the database...
            for(Track track : endMap.getTracks()) {
                if(track.getOwner() == playerId) {
                    int length = track.getLength();
                    switch (length) {
                        case 1:
                            builtTrainPoints = builtTrainPoints+1;
                            break;
                        case 2:
                            builtTrainPoints = builtTrainPoints+2;
                            break;
                        case 3:
                            builtTrainPoints = builtTrainPoints+4;
                            break;
                        case 4:
                            builtTrainPoints = builtTrainPoints+7;
                            break;
                        case 5:
                            builtTrainPoints = builtTrainPoints+10;
                            break;
                        case 6:
                            builtTrainPoints = builtTrainPoints+15;
                            break;
                    }
                }
            }
            //builtTrainPoints = player.getPoints();

            //Get the TrackForest Object
            TrackForest trackForest = new TrackForest(endMap.getTracks());
            Set<Integer> playersWithLongestTrack = trackForest.getPlayerWithLongestTrack();

            if(playersWithLongestTrack.contains(playerId)) {
                longestContinuousTrain = 10;
            }

            //get the player's Destination Cards
            //This always return zero...
            List<DestinationCard> playerCards = dbc.getPlayerDestinationCards(endModel.getName().toString(), player.getUsername());

            //Go through the player's Destination Cards and see if they have completed it
            for(DestinationCard card : playerCards) {
                if(trackForest.connectedCities(card.getCity1(), card.getCity2(), player.getPlayer())) {
                    completedDestinations = completedDestinations + card.getPointsWorth();
                } else {
                    incompleteDestinations = incompleteDestinations - card.getPointsWorth();
                }
            }

            total = builtTrainPoints + completedDestinations + incompleteDestinations + longestContinuousTrain;

            EndResult currentPlayerResults = new EndResult(playerId, builtTrainPoints, completedDestinations, incompleteDestinations, longestContinuousTrain, total);
            results.add(currentPlayerResults);
        }
        return results;
    }
}
