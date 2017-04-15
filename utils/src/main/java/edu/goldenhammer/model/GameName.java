package edu.goldenhammer.model;

/**
 * Created by seanjib on 3/4/2017.
 */
public class GameName {
    private String name;

    public GameName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
