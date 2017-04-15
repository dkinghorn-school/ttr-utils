package edu.goldenhammer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.server.Results;

/**
 * Created by seanjib on 2/5/2017.
 *
 * Response code indicates success
 */
public class LeaveGameHandler extends HandlerBase {
    public void handle(HttpExchange exchange) {
        try {
            IDatabaseController dbc = DatabaseController.getInstance();
            Results results = new Results();

            //verify username and access token
            if(!isAuthorized(exchange)) {
                results = getInvalidAuthorizationResults();
            }
            else {
                //Get games either for a specific user or get all the games at once
                if(exchange.getRequestHeaders().containsKey("username")
                        && exchange.getRequestHeaders().containsKey("gamename")) {
                    String username = exchange.getRequestHeaders().get("username").get(0);
                    String gamename = exchange.getRequestHeaders().get("gamename").get(0);

                    boolean success = dbc.leaveGame(username, gamename);

                    if (success) {
                        dbc.maybeDropGame(gamename);
                        results.setResponseCode(200);
                        results.setAndSerializeMessage("Game " + gamename + " successfully left!");
                    } else {
                        results.setResponseCode(400);
                        results.setAndSerializeMessage("Error: cannot leave game");
                    }
                }
                else {
                    results.setResponseCode(400);
                    results.setAndSerializeMessage("Error: Invalid username or game name included in URL");
                }
            }
            sendResponse(exchange, results);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
