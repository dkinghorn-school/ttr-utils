package edu.goldenhammer.server.handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

import java.io.IOException;

/**
 * Created by seanjib on 3/9/2017.
 */
public class PostMessageHandler extends HandlerBase {
    public void handle(HttpExchange exchange) {
        Results results = new Results();
        try {
            String requestBody = readRequestBody(exchange);
            IDatabaseController dbc = DatabaseController.getInstance();

            if(!isAuthorized(exchange)) {
                results = getInvalidAuthorizationResults();
            }
            else {
                JsonObject messageJson = Serializer.deserialize(requestBody);
                String message = messageJson.get("message").getAsString();

                String username = exchange.getRequestHeaders().get("username").get(0);
                String game_name = exchange.getRequestHeaders().get("gamename").get(0);
                boolean success = dbc.postMessage(game_name, username, message);

                if(success) {
                    results.setResponseCode(200);
                    results.setAndSerializeMessage("Message successfully posted to server!");
                }
                else {
                    results.setResponseCode(400);
                    results.setAndSerializeMessage("Error! Something went wrong!");
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        try {
            sendResponse(exchange, results);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
}
