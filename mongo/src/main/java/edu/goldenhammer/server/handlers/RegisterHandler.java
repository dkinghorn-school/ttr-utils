package edu.goldenhammer.server.handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

import java.io.IOException;
import java.util.Random;

public class RegisterHandler extends HandlerBase {
    public void handle(HttpExchange exchange) {
        Results result = new Results();
        result.setResponseCode(404);
        result.setAndSerializeMessage("Error: user exists");
        try {
            try {
                String requestBody = readRequestBody(exchange);
                JsonObject credentials = Serializer.deserialize(requestBody);
                String username = credentials.get("username").getAsString();
                String password = credentials.get("password").getAsString();

                IDatabaseController dbc = DatabaseController.getInstance();
                boolean success = dbc.createUser(username, password);


                if (success) {
                    dbc.setAccessToken(username, Integer.toString(new Random().nextInt(100000000)));
                    String access_token = dbc.getPlayerInfo(username).getAccessToken();
                    result.setResponseCode(200);
                    result.setMessage(String.format("{\"authorization\":\"%1$s\"}",access_token));
                }
            } catch (Exception e){
                result.setResponseCode(400);
                result.setAndSerializeMessage("Error: need username and password");
            }
            sendResponse(exchange, result);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}