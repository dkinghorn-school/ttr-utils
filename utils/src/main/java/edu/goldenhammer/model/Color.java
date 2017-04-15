package edu.goldenhammer.model;

/**
 * Created by seanjib on 3/2/2017.
 */
public enum Color {
    WILD, RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE, BLACK, WHITE, GRAY;

    public static Color getPlayerColorFromNumber(int colorNumber) {
        switch(colorNumber) {
            case 0:
                return BLUE;
            case 1:
                return RED;
            case 2:
                return GREEN;
            case 3:
                return YELLOW;
            case 4:
                return BLACK;
            default:
                return null;
        }
    }

    public static Color getTrainCardColorFromString(String color) {
        switch (color) {
            case "wild":
                return WILD;
            case "red":
                return RED;
            case "orange":
                return ORANGE;
            case "yellow":
                return YELLOW;
            case "green":
                return GREEN;
            case "blue":
                return BLUE;
            case "violet":
                return PURPLE;
            case "black":
                return BLACK;
            case "white":
                return WHITE;
            default:
                return null;
        }
    }

    public static Color getTrackColorFromString(String color) {
        switch (color) {
            case "gray":
                return GRAY;
            case "red":
                return RED;
            case "orange":
                return ORANGE;
            case "yellow":
                return YELLOW;
            case"green":
                return GREEN;
            case "blue":
                return BLUE;
            case "violet":
                return PURPLE;
            case "black":
                return BLACK;
            case "white":
                return WHITE;
            default:
                return null;
        }
    }
}
