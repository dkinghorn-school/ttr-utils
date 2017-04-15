package edu.goldenhammer.model;

/**
 * Created by seanjib on 3/4/2017.
 */
public class PlayerOverview {
    private Color color;
    private int pieces;
    private int destCards;
    private int player;
    private String username;
    private int points;

    public PlayerOverview() {
        color = null;
        pieces = -1;
        destCards = -1;
        player = -1;
        points = -1;
    }

    public PlayerOverview(Color color, int pieces, int destCards, int player, String username, int points) {
        this.color = color;
        this.pieces = pieces;
        this.destCards = destCards;
        this.player = player;
        this.username = username;
        this.points = points;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getPieces() {
        return pieces;
    }

    public void setPieces(int pieces) {
        this.pieces = pieces;
    }

    public int getDestCards() {
        return destCards;
    }

    public void setDestCards(int destCards) {
        this.destCards = destCards;
    }

    public int getPlayer() {
        return player;
    }

    public void setPlayer(int player) {
        this.player = player;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
