package edu.goldenhammer.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.model.Player;
import edu.goldenhammer.server.Results;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class HandlerBase implements HttpHandler {

    protected String readRequestBody(HttpExchange httpExchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader is = new InputStreamReader(httpExchange.getRequestBody());
        char[] buf = new char[1024];
        int length;
        while((length = is.read(buf)) > 0) {
            sb.append(buf, 0, length);
        }

        return sb.toString();
    }

    protected void sendResponse(HttpExchange httpExchange, Results results) throws IOException{
        String response = results.getMessage();
        httpExchange.getResponseHeaders().add("Content-Type","application/json");
        httpExchange.sendResponseHeaders(results.getResponseCode(), response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    protected boolean isAuthorized(HttpExchange exchange) {
        try {
            String access_token = exchange.getRequestHeaders().get("Authorization").get(0);
            String username = exchange.getRequestHeaders().get("Username").get(0);
            IDatabaseController dbc = DatabaseController.getInstance();
            Player player = dbc.getPlayerInfo(username);
            if (player == null) {
                return false;
            } else {
                return access_token.equals(player.getAccessToken());
            }
        } catch(NullPointerException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    protected Results getInvalidAuthorizationResults() {
        Results results = new Results();
        results.setResponseCode(403);
        results.setAndSerializeMessage("Device not authorized. Log in again");

        return results;
    }

    protected boolean isInGame(HttpExchange exchange) {
        String game_name = exchange.getRequestHeaders().get("gamename").get(0);
        String username = exchange.getRequestHeaders().get("username").get(0);
        IDatabaseController dbc = DatabaseController.getInstance();
        List<String> players = dbc.getPlayers(game_name);
        boolean containsUsername = players.contains(username);
        return containsUsername;
    }

    protected Results getNotInGameResults() {
        Results results = new Results();
        results.setResponseCode(400);
        results.setAndSerializeMessage("Error: you have not joined that game.");

        return results;
    }

    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        if(query == null){
            return result;
        }
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1]);
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }
}
