package edu.goldenhammer.server.commands;

import edu.goldenhammer.database.DatabaseController;
import edu.goldenhammer.database.IDatabaseController;
import edu.goldenhammer.database.IGameDAO;
import edu.goldenhammer.server.Results;

import java.io.Serializable;

public abstract class BaseCommand implements Serializable {
    private String name;
    private int playerNumber;
    private int commandNumber;
    private String gameName;
    private String playerName;

    public String getName() {
        return name;
    }

    public int getCommandNumber() {
        return commandNumber;
    }


    public void hide(){}
    protected final void setName(String name) {
        this.name = name;
    }

    public final void setCommandNumber(int commandNumber) {
        this.commandNumber = commandNumber;
    }

    public boolean validate() {
        IGameDAO dbc = DatabaseController.getGameDAO();
        return dbc.validateCommand(this);
    }

    public boolean isLastRound() {return false;}
    public boolean endTurn() {
        return true;
    }

    public boolean isEndOfGame() {
        return false;
    }

    public abstract Results execute();

    public int getPlayerNumber() {
        return playerNumber;
    }

    public void setPlayerNumber(int playerNumber) {
        this.playerNumber = playerNumber;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    protected void addToDatabase(boolean visibleToSelf, boolean visibleToAll) {
        IGameDAO dbc = DatabaseController.getGameDAO();
        dbc.addCommand(this, visibleToSelf, visibleToAll);
    }
}
