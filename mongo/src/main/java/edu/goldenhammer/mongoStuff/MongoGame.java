package edu.goldenhammer.mongoStuff;

import com.google.gson.*;
import edu.goldenhammer.model.*;
import edu.goldenhammer.server.Serializer;
import edu.goldenhammer.server.commands.BaseCommand;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by devonkinghorn on 4/10/17.
 */
public class MongoGame implements Serializable {
    GameModel checkpoint;
    int checkpointIndex;
    List<Message> chatMessages;
    List<BaseCommand> commands;
    List<String> players;
    String gameName;
    Map<String, Hand> hands = new TreeMap<>();
    List<DestinationCard> destDeck;
    List<DestinationCard> destDiscard;
    List<TrainCard> trainDeck;
    List<TrainCard> trainDiscard;

    public MongoGame(String name){
        commands = new ArrayList<>();
        chatMessages = new ArrayList<>();
        checkpoint = null;
        checkpointIndex = -1;
        players = new ArrayList<>();
        gameName = name;
        destDeck = new ArrayList<>();
        destDiscard = new ArrayList<>();
        trainDeck = new ArrayList<>();
        trainDiscard = new ArrayList<>();
    }

    public MongoGame(GameModel checkpoint, int checkpointIndex, List<Message> chatMessages, List<BaseCommand> commands, List<String> players) {
        if(commands == null)
            commands = new ArrayList<>();
        if(chatMessages == null)
            chatMessages = new ArrayList<>();

        this.checkpoint = checkpoint;
        this.checkpointIndex = checkpointIndex;
        this.chatMessages = chatMessages;
        this.commands = commands;
        this.players = players;
        this.gameName = checkpoint.getName().toString();
        destDeck = new ArrayList<>();
        destDiscard = new ArrayList<>();
        trainDeck = new ArrayList<>();
        trainDiscard = new ArrayList<>();
    }

    public GameModel getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(GameModel checkpoint) {
        this.checkpoint = checkpoint;
    }

    public int getCheckpointIndex() {
        return checkpointIndex;
    }

    public void setCheckpointIndex(int checkpointIndex) {
        this.checkpointIndex = checkpointIndex;
    }

    public List<Message> getChatMessages() {
        return chatMessages;
    }

    public void setChatMessages(List<Message> chatMessages) {
        this.chatMessages = chatMessages;
    }

    public List<BaseCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<BaseCommand> commands) {
        this.commands = commands;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public Map<String, Hand> getHands() {
        return hands;
    }

    public void setHands(Map<String, Hand> hands) {
        this.hands = hands;
    }

    public List<DestinationCard> getDestDeck() {
        return destDeck;
    }

    public void setDestDeck(List<DestinationCard> destDeck) {
        this.destDeck = destDeck;
    }

    public List<DestinationCard> getDestDiscard() {
        return destDiscard;
    }

    public void setDestDiscard(List<DestinationCard> destDiscard) {
        this.destDiscard = destDiscard;
    }

    public List<TrainCard> getTrainDeck() {
        return trainDeck;
    }

    public void setTrainDeck(List<TrainCard> trainDeck) {
        this.trainDeck = trainDeck;
    }

    public List<TrainCard> getTrainDiscard() {
        return trainDiscard;
    }

    public void setTrainDiscard(List<TrainCard> trainDiscard) {
        this.trainDiscard = trainDiscard;
    }

    protected static class BaseCommandAdapter implements JsonDeserializer<BaseCommand>, JsonSerializer<BaseCommand> {
        @Override
        public BaseCommand deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Serializer.deserializeCommand(json.toString(), "edu.goldenhammer.server.commands.");
            /*
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(json.toString(), JsonObject.class);
            String commandName = jsonObject.get("name").getAsString();

            String className = "edu.goldenhammer.server.commands." + commandName + "Command";

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
            */
        }

        @Override
        public JsonElement serialize(BaseCommand src, Type typeOfSrc, JsonSerializationContext context) {
            return null;
        }
    }
    public static MongoGame deserialize(String jsObject) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(BaseCommand.class, new BaseCommandAdapter());
        Gson gson = builder.create();
        return gson.fromJson(jsObject, MongoGame.class);
    }
}
