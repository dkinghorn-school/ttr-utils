package edu.goldenhammer.server.handlers;

import com.sun.net.httpserver.HttpExchange;

import edu.goldenhammer.server.CommandManager;
import edu.goldenhammer.server.Results;
import edu.goldenhammer.server.Serializer;
import edu.goldenhammer.server.commands.BaseCommand;

import java.io.IOException;
import java.util.List;

public class CommandHandler extends HandlerBase {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Results results = new Results();
        try {
            String requestBody = readRequestBody(httpExchange);
            if(!isAuthorized(httpExchange)) {
                results = getInvalidAuthorizationResults();
            }
            else if(!isInGame(httpExchange)) {
                results = getNotInGameResults();
            }
            else {
                String pkg = "edu.goldenhammer.server.commands.";
                BaseCommand baseCommand = Serializer.deserializeCommand(
                        requestBody, pkg/* + ".Server"*/);
                String game_name = httpExchange.getRequestHeaders().get("gamename").get(0);
                String player_name = httpExchange.getRequestHeaders().get("username").get(0);
                baseCommand.setGameName(game_name);
                baseCommand.setPlayerName(player_name);

                CommandManager comManager = new CommandManager();
                List<BaseCommand> commands = comManager.addCommand(baseCommand);
                if(commands.size() > 0) {
                    results.setResponseCode(200);
                    results.setMessage(Serializer.serialize(commands));
                }
                else {
                    results.setResponseCode(400);
                    results.setMessage("Error: invalid command entered");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            results.setResponseCode(500);
            results.setMessage("Error: an error occurred.");
        }
        sendResponse(httpExchange, results);
    }
}
