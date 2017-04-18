package edu.goldenhammer.mongoStuff;

import com.mongodb.*;
import com.mongodb.util.JSON;
import edu.goldenhammer.model.Message;
import edu.goldenhammer.server.Serializer;
import edu.goldenhammer.server.commands.BaseCommand;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class MongoDriver {
    private static MongoClient mongoClient;
    private static MongoClient getClient() throws UnknownHostException{
        if(mongoClient == null) {
            mongoClient = new MongoClient("localhost");
//            DBCollection coll = mongoClient.getDB("ticket_to_ride").getCollection("games");
//
        }
        return mongoClient;
    }
    public MongoDriver() {

    }

    private DBCollection getCollection(String collectionName) throws UnknownHostException{
        return getClient().getDB("ticket_to_ride").getCollection(collectionName);
    }

    private DBCollection getGameCollection() throws UnknownHostException {
        return getCollection("games");
    }

    private DBCollection getUserCollection() throws UnknownHostException {
        return getCollection("user");
    }

    public boolean removeGame(String gameName) throws UnknownHostException {
        DBCollection coll = getGameCollection();
        DBObject query = new BasicDBObject("gameName", gameName);
        return coll.remove(query).getN() == 1;
    }

    private DBObject getOne(Cursor cursor) {
        if(cursor.hasNext())
            return cursor.next();
        return null;
    }
    private List<MongoGame> getGamesFromCursor(DBCursor cursor) {
        ArrayList<MongoGame> games = new ArrayList<>();
        while(cursor.hasNext()) {
            games.add(MongoGame.deserialize(cursor.next().toString()));
        }
        return games;
    }

    public List<MongoGame> getGamesWithPlayer(String username) throws UnknownHostException{
        DBCollection coll = getGameCollection();
        ArrayList<String> s = new ArrayList<>();
        s.add(username);
        DBObject query = new BasicDBObject("players", new BasicDBObject("$in", s));
        DBCursor cursor = coll.find(query);
        return getGamesFromCursor(cursor);
    }

    public boolean addChat(String gameName, Message chat) throws UnknownHostException {
        DBObject json = (DBObject) JSON.parse(Serializer.serialize(chat));
        DBCollection coll = getGameCollection();
        DBObject query = new BasicDBObject("gameName", new BasicDBObject("$eq",gameName));
        DBObject push = new BasicDBObject("$push", new BasicDBObject("chatMessages",json));
        return coll.update(query, push).getN() == 1;
    }

    /**
     *
     * @param username games excluded that have this user
     * @return
     */
    public List<MongoGame> getGamesNotStartedWithoutPlayer(String username) throws UnknownHostException {
        DBCollection coll = getGameCollection();
        ArrayList<String> s = new ArrayList<>();
        s.add(username);
        DBObject query1 = new BasicDBObject("checkpoint", new BasicDBObject("$exists", "false"));
        DBObject query2 = new BasicDBObject("players", new BasicDBObject("$not", new BasicDBObject("$in", s)));
        BasicDBList and = new BasicDBList();
        and.add(query1);
        and.add(query2);
        DBObject query = new BasicDBObject("$and", and);
        DBCursor cursor = coll.find(query);
        return getGamesFromCursor(cursor);
    }


    public List<MongoGame> getAllGames() throws UnknownHostException {
        DBCollection coll = getGameCollection();
        //TODO: check the query to make sure it returns all of the games.
        DBObject query = new BasicDBObject();
  DBCursor cursor = coll.find(query);
        return getGamesFromCursor(cursor);
    }

    public List<MongoGame> getGamesNotStarted() throws UnknownHostException {
        DBCollection coll = getGameCollection();
        DBObject query = new BasicDBObject("checkpoint", new BasicDBObject("$exists", "false"));
        DBCursor cursor = coll.find(query);
        return getGamesFromCursor(cursor);
    }

    /**
     *
     * @param gameName
     * @return the json what is contained in mongo
     * @throws UnknownHostException
     */
    public MongoGame getGame(String gameName) throws UnknownHostException{
        DBCollection coll = getGameCollection();
        BasicDBObject query = new BasicDBObject("gameName", gameName);
        Cursor cursor = coll.find(query);
        DBObject object = getOne(cursor);
        if(object != null) {
            return MongoGame.deserialize(object.toString());
        } else {
            return null;
        }
    }
    //games player not in
    //games player is in
    //get checkpoint
    //addSingleCommand

    public boolean setGame(MongoGame game) throws UnknownHostException {
        DBCollection coll = getGameCollection();
        DBObject json = (DBObject) JSON.parse(Serializer.serialize(game));
        DBObject query = new BasicDBObject("gameName", new BasicDBObject("$eq",game.gameName));
        DBObject update = new BasicDBObject();
        update.put("$set", json);
        return coll.update(query, update, true, false).getN() == 1;
    }

    public boolean setUser(MongoUser user) throws UnknownHostException {
        DBCollection coll = getUserCollection();
        DBObject json = (DBObject) JSON.parse(Serializer.serialize(user));
        DBObject query = new BasicDBObject("username", new BasicDBObject("$eq",user.getUsername()));
        DBObject update = new BasicDBObject();
        update.put("$set", json);
        return coll.update(query, update, true, false).getN() == 1;
    }

    public MongoUser getUser(String username) throws UnknownHostException {
        DBCollection coll = getUserCollection();
        BasicDBObject query = new BasicDBObject("username", username);
        Cursor cursor = coll.find(query);
        DBObject object = getOne(cursor);
        if(object != null) {
            return MongoUser.deserialize(object.toString());
        } else {
            return null;
        }
    }

    public boolean addSingleCommand(BaseCommand command, String gameName) throws UnknownHostException{
        DBObject json = (DBObject) JSON.parse(Serializer.serialize(command));
        DBCollection coll = getGameCollection();
        DBObject query1 = new BasicDBObject("gameName", new BasicDBObject("$eq",gameName));
        DBObject query2 = new BasicDBObject("commands", new BasicDBObject("$size",command.getCommandNumber()));
        BasicDBList and = new BasicDBList();
        and.add(query1);
        and.add(query2);
        DBObject query = new BasicDBObject("$and", and);
        DBObject push = new BasicDBObject("$push", new BasicDBObject("commands",json));
        return coll.update(query, push).getN() == 1;
    }

    public void clearAll() throws UnknownHostException {
        DBCollection coll = getGameCollection();
        DBObject query = new BasicDBObject();
        coll.remove(query);
        coll = getUserCollection();
        coll.remove(query);
    }



}
