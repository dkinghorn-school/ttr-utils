package edu.goldenhammer.server.commands;

import java.util.List;

import edu.goldenhammer.model.EndResult;
import edu.goldenhammer.model.PlayerOverview;
import edu.goldenhammer.model.ResultsGenerator;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

/**
 * Created by jon on 3/22/17.
 */

public class EndGameCommand extends BaseCommand {
    int winner;
    //Are these results the stats for all the players?
    // Spec: Results[] results
    List<EndResult>  playersResults; //get these from the database...
    
    //Results results
    public EndGameCommand(String gameName) {
        setName("EndGame");
        setGameName(gameName);
    }

    @Override
    public Results execute() {
        Results result = new Results();
        ResultsGenerator resultsGenerator = new ResultsGenerator();
        playersResults = resultsGenerator.generateResults(getGameName());
        result.setMessage(Serializer.serialize(playersResults));
        addToDatabase(true, true);
        return result;
    }
}