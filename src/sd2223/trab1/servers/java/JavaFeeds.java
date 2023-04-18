package sd2223.trab1.servers.java;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;

import java.sql.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaFeeds implements Feeds {

    /**
     * Map of message by user.
     */
    private final Map<String, List<Message>> userMessages = new HashMap<>();

    /**
     * Map of users subscribed by user.
     */
    private final Map<String, List<String>> users = new HashMap<>();

    private static Logger Log = Logger.getLogger(JavaFeeds.class.getName());


    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        Log.info("postMessage: user = " + user + ", msg = " + msg);

        //Check if user is valid
        if (user == null || pwd == null || msg == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //Check if msg is valid
        if (msg.getId() <= 0 || msg.getUser() == null || msg.getText() == null || msg.getCreationTime() <= 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }




        //Check if user exists
        if(!userMessages.containsKey(user)) {
            userMessages.put(user, new LinkedList<>());
            userMessages.get(user).add(msg);
        }else
            userMessages.get(user).add(msg);
        return Result.ok(msg.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        Log.info("removeFromPersonalFeed: user = " + user + ", mid = " + mid);

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
        }
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info("getMessage: user = " + user + ", mid = " + mid);

        //Check if user and mid is valid
        if (user == null && mid <= 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        //check if user exists
        if(!userMessages.containsKey(user)) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }else{
            List<Message> messages = userMessages.get(user);
            //TODO better way to do this?
            for (Message message : messages) {
                if(message.getId() == mid){
                    return Result.ok(message);
                }
            }
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

    }
    @Override
    public Result<List> getMessages(String user, long time) {
        Log.info("getMessages: user = " + user + ", time = " + time);

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
        }
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return null;
    }
      /*  Log.info("subUser: user = " + user + ", userSub = " + userSub);

        //Check if user, the userSub and the pwd is valid
        if (user == null || userSub == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //Check if user and the userSub exist
        if (user == null || userSub == null) {
            Log.info("UserId or Password null.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        //Check if user is valid
        if (pwd == null) {
            Log.info("Password null.");
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        List<User> subs = users.get(user);
        if (subs == null) {
            subs = new ArrayList<User>();
            users.put(user, subs);

            User userUserSub= new User(userSub);
        }
        subs.add(userUserSub));

        return Result.ok();*/
   // }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return null;
    }
     /*   //Check if user and the userSub exist
        if (user == null || userSub == null) {
            Log.info("UserId or Password null.");
            Result.error(Result.ErrorCode.NOT_FOUND);
            //throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        //Check if user is valid
        if (pwd == null) {
            Log.info("Password null.");
            Result.error(Result.ErrorCode.BAD_REQUEST);
            //throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        List<User> subs = users.get(user);

        if(subs.removeIf(s->s.getName().equals(userSub))){
            return Result.ok();
        }else{
            Result.error(Result.ErrorCode.NOT_FOUND);
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }*/

    @Override
    public Result<List> listSubs(String user) {

        //Check if usr is valid
        if (user == null) {
            Log.info("User null.");
            Result.error(Result.ErrorCode.BAD_REQUEST);
            //throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        //Check if user exists
        if (!users.containsKey(user)) {
            Log.info("User does not exist.");
            Result.error(Result.ErrorCode.NOT_FOUND);
            //throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

            List<String> subs = users.get(user);
            return Result.ok(subs);
    }
}

