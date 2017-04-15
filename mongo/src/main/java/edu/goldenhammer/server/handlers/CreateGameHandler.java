package edu.goldenhammer.server.handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

/**
 * Created by seanjib on 2/5/2017.
 */
public class CreateGameHandler extends HandlerBase {
    public void handle(HttpExchange exchange) {
        try {
            String requestBody = readRequestBody(exchange);

            IDatabaseController dbc = DatabaseController.getInstance();
            Results results = new Results();

            //verify username and access token
            if(!isAuthorized(exchange)) {
                results = getInvalidAuthorizationResults();
            }
            else {
                JsonObject gameNameJson = Serializer.deserialize(requestBody);
                String gameName = gameNameJson.get("name").getAsString();

                boolean success = dbc.createGame(gameName);

                if (success) {
                    String username = exchange.getRequestHeaders().get("username").get(0);
                    dbc.joinGame(username, gameName);
                    results.setResponseCode(200);
                    results.setAndSerializeMessage("Game " + gameName + " successfully created!");
                } else {
                    results.setResponseCode(400);
                    results.setAndSerializeMessage("Error: game already exists");
                }
            }
            sendResponse(exchange, results);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}