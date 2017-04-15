package edu.goldenhammer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;
import edu.goldenhammer.server.commands.BaseCommand;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by seanjib on 3/4/2017.
 */
public class GetCommandsHandler extends HandlerBase {

    public void handle(HttpExchange exchange) {
        Results results = new Results();
        try {
            if(!isAuthorized(exchange)) {
                results = getInvalidAuthorizationResults();
            }
            else if(!isInGame(exchange)) {
                results = getNotInGameResults();
            }
            else {
                IDatabaseController dbc = DatabaseController.getInstance();
                List<BaseCommand> commandList;

                Map<String, String> parameters = queryToMap(exchange.getRequestURI().getQuery());

                String username = exchange.getRequestHeaders().get("Username").get(0);
                int lastCommandID = Integer.parseInt(parameters.get("lastCommand"));
                String game_name = parameters.get("gamename");

                commandList = dbc.getCommandsSinceLastCommand(game_name, username, lastCommandID);

                //Make sure commandList was actually populated with data, even if there are no commands
                if(commandList == null) {
                    results.setResponseCode(400);
                    results.setAndSerializeMessage("ERROR: you fool! you gave me bad parameters, I guess.");
                }
                else {
                    results.setResponseCode(200);
                    results.setMessage(Serializer.serialize(commandList));
                }
            }
            sendResponse(exchange, results);
        } catch (IOException e) {
            e.printStackTrace();
        }
        results.setResponseCode(400);
        results.setAndSerializeMessage("ERROR: You done messed up");
    }
}
