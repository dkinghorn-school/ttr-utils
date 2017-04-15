package edu.goldenhammer.mongoStuff;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by devonkinghorn on 4/12/17.
 */
public class MongoUser implements Serializable {
    private String username;
    private String password;
    private String token;

    public MongoUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static MongoUser deserialize(String jsObject) {
        Gson gson = new Gson();
        return gson.fromJson(jsObject, MongoUser.class);
    }
}
