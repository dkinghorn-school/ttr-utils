package edu.goldenhammer.model;

/**
 * Created by seanjib on 3/2/2017.
 */
public class Player {
    private String username;
    private String accessToken;

    public Player(String username, String accessToken) {
        this.username = username;
        this.accessToken = accessToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
