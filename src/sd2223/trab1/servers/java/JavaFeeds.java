package sd2223.trab1.servers.java;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.clients.UsersClientFactory;

import sd2223.trab1.servers.rest.RestUsersServer;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static sd2223.trab1.clients.UsersClientFactory.getUserClient;

public class JavaFeeds implements Feeds {

    public static final String USERS_SERVICE = "UsersService";

    /**
     * Map of message by user.
     */
    private final Map<String, List<Message>> userMessages;

    /**
     * Map of users subscribed by user.
     */
    private final Map<String, List<String>> usersSubscribed;

    private static Logger Log;

    private static long msgID;

    private Discovery discovery;

    public JavaFeeds() {
        userMessages = new ConcurrentHashMap<>();
        usersSubscribed = new ConcurrentHashMap<>();
        Log = Logger.getLogger(JavaFeeds.class.getName());
        msgID = 1;
        discovery = Discovery.getInstance();
    }

    private Result<URI> hasDomain(String user) {
        var parts = user.split("@");
        String name = parts[0];
        String domain = parts[1];
        String serviceName = domain + ":" + USERS_SERVICE;
        URI[] uris = discovery.knownUrisOf(serviceName, 1);
        if (uris.length == 0) {
            Log.info("No known URI for service " + serviceName);
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        Log.info("______Found URI " + uris[0] + " for service ____ " + serviceName);
        return Result.ok(uris[0]);
    }

    private Result<User> isUserValid(String user, String pwd) {
        Result<URI> r = hasDomain(user);
        Log.info("______URI " + r.value() + " ____");
        if(!r.isOK())
            return Result.error(r.error());
        URI uri = r.value();
        var parts = user.split("@");
        String name = parts[0];
        return UsersClientFactory.getUserClient(uri).getUser(name, pwd);
    }






    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        Log.info("postMessage: user = " + user + ", msg = " + msg);

        //Check if user is valid
        if (user == null || pwd == null || msg == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Result u = isUserValid(user, pwd);

        //Check if user exists
        if(!u.isOK()) {
            Log.info("User not found or wrong password");
            return Result.error(u.error());
        }

        msg.setId(msgID++);

        //Check if user has any messages
        if(!userMessages.containsKey(user)) {
            List<Message> messages = new ArrayList<>();
            messages.add(msg);
            userMessages.put(user, messages);
        }
        else{
            userMessages.get(user).add(msg);
        }

        return Result.ok(msg.getId());
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info("getMessage: user = " + user + ", mid = " + mid);

        if (user == null && mid <= 0)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        if(!userMessages.containsKey(user))
            return Result.error(Result.ErrorCode.NOT_FOUND);

        List<Message> messages = userMessages.get(user);
        for (Message message : messages) {
            if(message.getId() == mid){
                Log.info("Message found: " + message);
                return Result.ok(message);
            }
        }
        Log.info("Message not found");
        return Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return null;
    }
       /* Log.info("removeFromPersonalFeed: user = " + user + ", mid = " + mid);

        //Check if user is valid
        if (user == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        //Check if user exists
        if(!userMessages.containsKey(user)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }else{
            List<Message> messages = userMessages.get(user);
            //TODO better way to do this?
            for (Message message : messages) {
                if(message.getId() == mid){
                    messages.remove(message);
                    return Result.ok();
                }
            }
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }*/

    @Override
    public Result<List> getMessages(String user, long time) {
        Log.info("GetMessages: user = " + user + ", time = " + time);
        List<String> subs =  usersSubscribed.get(user);

        //Check if user and time is valid
        if (user == null && time <= 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //check if user exists
        if(!userMessages.containsKey(user))
            return Result.error(Result.ErrorCode.NOT_FOUND);

       /* Result r1 = hasDomain(user);
        if(!r1.isOK()) {
            Log.info("User not found");
            return Result.error(r1.error());
        }*/

        List<Message> newMessages = new LinkedList<>();
        //Add their new messages
        List<Message> messages = userMessages.get(user);
        //Check if user has any messages
        if(messages != null){
            for (Message message : messages) {
                if(message.getCreationTime() > time){
                    newMessages.add(message);
                }
            }
        }
        //Check if user has any subscribed users
        if(subs != null){
            //Add their subscribed users new messages
            for (String sub : subs) {
                List<Message> subMessages = userMessages.get(sub);
                //Check if user has any messages
                if(subMessages != null){
                    for (Message message : subMessages) {
                        if(message.getCreationTime() > time){
                            newMessages.add(message);
                        }
                    }
                }
            }
        }

        return Result.ok(newMessages);

       /* Log.info("getMessages: user = " + user + ", time = " + time);

        //Check if user and time is valid
        if (user == null && time <= 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //check if user exists
        if(!userMessages.containsKey(user)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }else{
            List<Message> messages = userMessages.get(user);
            List<Message> messagesToSend = new LinkedList<>();
            for (Message message : messages) {
                if(message.getCreationTime() > time){
                    messagesToSend.add(message);
                }
            }
            return Result.ok(messagesToSend);
        }*/
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        Log.info("Sub a User: user = " + user + ", userSub = " + userSub);
        //Check if user, the userSub and the pwd is valid
        if (user == null || userSub == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //Check if user is valid
        Result r = isUserValid(user, pwd);
        if(!r.isOK()) {
            Log.info("User not found or wrong password");
            return Result.error(r.error());
        }

        //Check if userSub exists
        Result r2 = hasDomain(userSub);
        if(!r2.isOK()) {
            Log.info("UserSub not found");
            return Result.error(r2.error());
        }

        List subs = usersSubscribed.get(user);
        if(subs == null) {
            subs = new ArrayList();
            subs.add(userSub);
            usersSubscribed.put(user, subs);
        }
        else{
            if(subs.contains(userSub)) {
                return Result.error(Result.ErrorCode.BAD_REQUEST);
            }
            else {
                subs.add(userSub);
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        Log.info("unsubscribeUser: user = " + user + ", userSub = " + userSub);
        //Check if user, the userSub and the pwd is valid
        if (user == null || userSub == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        //Check if user is valid
        Result r = isUserValid(user, pwd);
        if(!r.isOK()) {
            Log.info("User not found or wrong password");
            return Result.error(r.error());
        }
        //Check if userSub exists
        Result r2 = hasDomain(userSub);
        if(!r2.isOK()) {
            Log.info("UserSub not found");
            return Result.error(r2.error());
        }
        List subs = usersSubscribed.get(user);
        if(subs == null) {
            Log.info("UserSub not subscribed");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        else{
            if(subs.contains(userSub)) {
                subs.remove(userSub);
            }
            else {
                Log.info("UserSub not subscribed");
                return Result.error(Result.ErrorCode.BAD_REQUEST);
            }
        }

        return Result.ok();
    }

    @Override
    public Result<List> listSubs(String user) {

        //Check if usr is valid
        if (user == null) {
            Log.info("User null.");
            Result.error(Result.ErrorCode.BAD_REQUEST);
            //throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        //Check if user exists
        if (!usersSubscribed.containsKey(user)) {
            Log.info("User does not exist.");
            Result.error(Result.ErrorCode.NOT_FOUND);
            //throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        List<String> subs = usersSubscribed.get(user);
        if(subs == null) {
            subs = new ArrayList<>();
        }
        return Result.ok(subs);
    }
}

