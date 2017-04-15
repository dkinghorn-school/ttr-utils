package edu.goldenhammer.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import edu.goldenhammer.server.commands.BaseCommand;

public class Serializer {
    public static String serialize(Object c) {
        Gson gson = new Gson();
        return gson.toJson(c);
    }

    public static JsonObject deserialize(String json) {
        JsonParser parser = new JsonParser();
        return parser.parse(json).getAsJsonObject();
    }

    public static BaseCommand deserializeCommand(String json, String packagePrefix) {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        String commandName = jsonObject.get("name").getAsString();

        String className = packagePrefix + commandName + "Command";

        BaseCommand basecmd = null;
        try {
            Class c = null;
            try {
                c = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            basecmd = (BaseCommand)gson.fromJson(json, c);

        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return basecmd;
    }
}
