package edu.goldenhammer.server;

import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.database.Lock;
import edu.goldenhammer.server.commands.*;

/**
 * Created by root on 3/15/17.
 *
 * Check command type
 * Execute all the commands and put them into the database, and execute them.
 */

public class CommandManager {

    /**
     * Add the basecommand, and then validate and execute
     * Check command type,
     * @param command
     * @return all the commands, such as base command, and endturn, null if not allowed
     */

    public List<BaseCommand> addCommand(BaseCommand command) {
        synchronized (Lock.getInstance().getLock(command.getGameName())) {
            IDatabaseController dbc = DatabaseController.getInstance();
            List<BaseCommand> executed = new ArrayList<>();
            int currentPlayer = currentPlayerTurn(command.getGameName(), command.getPlayerName());

            if(currentPlayer == command.getPlayerNumber() || (currentPlayer == -1 && command instanceof ReturnDestCardsCommand)) {
                if (command.validate()) {
                    command.execute();
                    executed.add(command);
                    if(dbc.isEndOfGame(command.getGameName())) {
                        EndGameCommand endGameCommand = new EndGameCommand(command.getGameName());
                        endGameCommand.setCommandNumber(command.getCommandNumber()+1);
                        endGameCommand.setPlayerName(command.getPlayerName());
                        endGameCommand.setPlayerNumber(command.getPlayerNumber());
                        endGameCommand.setGameName(command.getGameName());
                        endGameCommand.execute();
                        executed.add(endGameCommand);
                    } else {
                        if (command.endTurn()) {
                            EndTurnCommand endTurn = dbc.getEndTurnCommand(command.getGameName(), command.getCommandNumber() + 1, command.getPlayerName());
                            endTurn.execute();
                            executed.add(endTurn);
                        }
                        if (command.isLastRound()) {
                            LastTurnCommand lastTurnCommand = new LastTurnCommand();
                            lastTurnCommand.setGameName(command.getGameName());
                            lastTurnCommand.setPlayerName(command.getPlayerName());
                            lastTurnCommand.setPlayerNumber(command.getPlayerNumber());
                            lastTurnCommand.setCommandNumber(executed.get(executed.size() - 1).getCommandNumber() + 1);
                            lastTurnCommand.execute();
                            executed.add(lastTurnCommand);
                        }
                    }
                }
            }
            return executed;
        }
    }

    private int currentPlayerTurn(String game_name, String playerName) {
        IDatabaseController dbc = DatabaseController.getInstance();
        //-1 means that the not everyone has initialized their hands
        List<BaseCommand> commands = dbc.getCommandsSinceLastCommand(game_name, playerName, 0);
        int current_player = -1;
        for(BaseCommand command: commands) {
            if(command instanceof EndTurnCommand)
                current_player = ((EndTurnCommand) command).getNextPlayer();
        }

        return current_player;
    }
}
