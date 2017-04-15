package edu.goldenhammer.server.handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;

import java.io.IOException;
import java.util.Random;

/**
 * Created by seanjib on 2/5/2017.
 */
public class LoginHandler extends HandlerBase {
    public void handle(HttpExchange exchange) {
        Results results = new Results();
        results.setAndSerializeMessage("Error: bad credentials");
        results.setResponseCode(400);
        try {
            String requestBody = readRequestBody(exchange);
            JsonObject credentials = Serializer.deserialize(requestBody);
            String username = credentials.get("username").getAsString();
            String password = credentials.get("password").getAsString();

            IDatabaseController dbc = DatabaseController.getInstance();
            boolean success = dbc.login(username, password);


            if(success) {
                //todo: maybe we should always create a new accessToken
                dbc.setAccessToken(username, Integer.toString(new Random().nextInt(100000000)));
                String access_token = dbc.getPlayerInfo(username).getAccessToken();

                if(!access_token.isEmpty()) {
                    results.setResponseCode(200);
                    results.setMessage(String.format("{\"authorization\":\"%1$s\"}",access_token));
                }
            }

        } catch (Exception ex) {

        }
        try{
            sendResponse(exchange, results);
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
