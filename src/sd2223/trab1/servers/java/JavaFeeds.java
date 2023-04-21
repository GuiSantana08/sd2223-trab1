package sd2223.trab1.servers.java;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.Uri;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.clients.UsersClientFactory;

import sd2223.trab1.servers.rest.RestFeedsServer;
import sd2223.trab1.servers.rest.RestUsersServer;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import sd2223.trab1.servers.soap.SoapFeedsServer;

import static sd2223.trab1.clients.UsersClientFactory.getUserClient;

public class JavaFeeds implements Feeds {

    public static final String USERS_SERVICE = "users";

    public static final String FEEDS_SERVICE = "feeds";

    public static String DOMAIN;

    /**
     * Map of message by user.
     */
    private final Map<String, List<Message>> userMessages;

    private final Map<Long, Message> allMessages;

    /**
     * Map of users subscribed by user.
     */
    private final Map<String, List<String>> usersSubscribed;

    private static Logger Log;

    private static long msgID;

    private Discovery discovery;

    public JavaFeeds(String DOMAIN) {
        userMessages = new ConcurrentHashMap<>();
        usersSubscribed = new ConcurrentHashMap<>();
        allMessages = new ConcurrentHashMap<>();
        Log = Logger.getLogger(JavaFeeds.class.getName());
        msgID = 1;
        discovery = Discovery.getInstance();
        DOMAIN = this.DOMAIN;
    }

    private Result<User> isUserValid(String user, String pwd) {
        var parts = user.split("@");
        String name = parts[0];
        String domain = parts[1];
        String serviceName = domain + ":" + USERS_SERVICE;
        URI[] uris = discovery.knownUrisOf(serviceName, 1);
        if (uris.length == 0) {
            // //Log.info("No known URI for service " + serviceName);
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        URI uri = uris[0];
        Result<User> r = UsersClientFactory.getUserClient(uri).getUser(name, pwd);
        if (r.error().equals(Result.ErrorCode.NOT_FOUND))
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        else if (!r.isOK())
            return Result.error(r.error());
        return r;

    }

    private Result<Boolean> hasUser(String user) {
        var parts = user.split("@");
        String name = parts[0];
        String domain = parts[1];
        String serviceName = domain + ":" + USERS_SERVICE;
        URI[] uris = discovery.knownUrisOf(serviceName, 1);
        if (uris.length == 0) {
            // //Log.info("No known URI for service " + serviceName);
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        URI uri = uris[0];
        // //Log.info("PROCURRAAAAAA USERRRRRRRRRR");
        Result<List> r1 = UsersClientFactory.getUserClient(uri).searchUsers(name);
        if (!r1.isOK())
            return Result.error(r1.error());
        // //Log.info("DEVOLVEUUUUUU LISTTAAAAAAAAA");
        List<User> users = r1.value();
        // //Log.info("VERIFICAAAAAAAA USERRRRRRRRRRRR");
        if (users.size() == 0)
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        return Result.ok(true);
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        // //Log.info("postMessage: user = " + user + ", msg = " + msg);

        // Check if user is valid
        if (user == null || pwd == null || msg == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        var parts = user.split("@");
        String domain = parts[1];

        Log.info("userDom " + domain + " DOMAIN " + DOMAIN);
        // Check if user is in domain
        if (!domain.equals(DOMAIN)) {
            Log.info("SOAPPP EM RESTT");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        Result v = hasUser(user);
        if (!v.isOK()) {
            // //Log.info("User not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        Result u = isUserValid(user, pwd);

        // Check if user exists
        if (!u.isOK()) {
            // //Log.info("User not found or wrong password");

            // //Log.info("+++++++++++++" + u.error().toString() + "++++++++++++++");
            return Result.error(u.error());
        }

        msg.setId(msgID++);

        // Check if user has any messages
        if (!userMessages.containsKey(user)) {
            List<Message> messages = new ArrayList<>();
            messages.add(msg);
            userMessages.put(user, messages);
        } else {
            userMessages.get(user).add(msg);
        }
        allMessages.put(msg.getId(), msg);

        return Result.ok(msg.getId());
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        // Log.info("getMessage: user = " + user + ", mid = " + mid);
        // Log.info("DOMAIN: " + DOMAIN);
        var parts = user.split("@");
        String name = parts[0];
        String domain = parts[1];

        if (user == null && mid <= 0)
            return Result.error(Result.ErrorCode.BAD_REQUEST);

        // check if user exists
        Result r1 = hasUser(user);
        if (!r1.isOK()) {
            // //Log.info("User not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Check if user is in domain
        if (!domain.equals(DOMAIN)) {
            Log.info("MESSAGE IS NOT IN DOMAIN");
            String serviceName = domain + ":" + FEEDS_SERVICE;
            URI[] uris = discovery.knownUrisOf(serviceName, 1);
            if (uris.length != 0) {
                URI uri = uris[0];
                Result<Message> r = UsersClientFactory.getFeedsClient(uri).getMessage(user, mid);
                if (r.isOK())
                    return r;
            }

        }

        // Check if user has the message
        List<Message> messages = userMessages.get(user);
        if (messages != null) {
            for (Message message : messages) {
                if (message.getId() == mid) {
                    // //Log.info("Message found: " + message);
                    return Result.ok(message);
                }
            }

        }

        // Check if user is subscribed to the message
        List<String> usersID = usersSubscribed.get(user);
        // Log.info("SUBSS: " + usersID);
        if (usersID == null) {
            // Log.info("User not subscribed to any user");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        for (String userID : usersID) {
            // Log.info("Get user messaeges in other domain");
            var partsSub = userID.split("@");
            String nameSub = partsSub[0];
            String domainSub = partsSub[1];
            if (!domain.equals(domainSub)) {
                // Get user messaeges in other domain
                // Log.info("Get user messaeges in other domain");
                // Log.info( "nameSus: " + nameSub + " domain: " + domain + " domainSub: " +
                // domainSub);
                String serviceName = domainSub + ":" + FEEDS_SERVICE;
                URI[] uris = discovery.knownUrisOf(serviceName, 1);
                if (uris.length != 0) {
                    URI uri = uris[0];
                    // Log.info("URI: " + uri);
                    Result r2 = UsersClientFactory.getFeedsClient(uri).getOwnMessage(userID, mid);
                    // Log.info("Result: " + r2);
                    if (r2.isOK()) {
                        // //Log.info("Message found: " + r2.value());
                        Message m = (Message) r2.value();
                        return Result.ok(m);
                    }
                }
            } else {
                // Check if user has the message
                List<Message> messages2 = userMessages.get(userID);
                if (messages2 != null) {
                    for (Message message : messages2) {
                        if (message.getId() == mid) {
                            // //Log.info("Message found: " + message);
                            return Result.ok(message);
                        }
                    }

                }

            }
        }

        // //Log.info("Message not found");
        return Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        // //Log.info("removeFromPersonalFeed: user = " + user + ", mid = " + mid);

        // Check if user is valid
        if (user == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if mid is valid
        if (mid < 0) {
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Check if user exists
        Result r1 = isUserValid(user, pwd);
        if (!r1.isOK()) {
            // //Log.info("User not found");
            return Result.error(r1.error());
        }

        // Check if user has the message and remove it
        List<Message> messages = userMessages.get(user);
        if (messages != null) {
            for (Message message : messages) {
                if (message.getId() == mid) {
                    messages.remove(message);
                    return Result.ok();
                }
            }
        }

        Log.info("NAO CHEGA AQUII");
        return Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<List> getMessages(String user, long time) {
        // //Log.info("GetMessages: user = " + user + ", time = " + time);
        var parts = user.split("@");
        String domain = parts[1];
        List<String> subs = usersSubscribed.get(user);

        // Check if user and time is valid
        if (user == null && time < 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        // check if user exists
        Result r1 = hasUser(user);
        if (!r1.isOK()) {
            // //Log.info("User not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        // Check if user is in domain
        if (!domain.equals(DOMAIN)) {
            Log.info("MESSAGE IS NOT IN DOMAIN");
            String serviceName = domain + ":" + FEEDS_SERVICE;
            URI[] uris = discovery.knownUrisOf(serviceName, 1);
            if (uris.length != 0) {
                URI uri = uris[0];
                Result<List> r = UsersClientFactory.getFeedsClient(uri).getMessages(user, time);
                if (r.isOK())
                    return r;
            }

        }

        List<Message> newMessages = new LinkedList<>();
        // Add their new messages
        List<Message> messages = userMessages.get(user);
        // Check if user has any messages
        if (messages != null) {
            for (Message message : messages) {
                if (message.getCreationTime() > time) {
                    newMessages.add(message);
                }
            }
        }
        // Check if user has any subscribed users
        if (subs != null) {
            // Add their subscribed users new messages
            for (String sub : subs) {
                var partsSub = sub.split("@");
                String nameSub = partsSub[0];
                String domainSub = partsSub[1];
                // //Log.info("///////////////// Domain: " + domain + "NameSub" + nameSub + "
                // DomainSub: " + domainSub + "+++++++++++++++++++++++++++++++++++");
                if (!domain.equals(domainSub)) {
                    // //Log.info("++++++++++++++++++++++++++++++++Get messages from other
                    // domain++++++++++++++++++++++");
                    // Get user messaeges in other domain
                    String serviceName = domainSub + ":" + FEEDS_SERVICE;
                    URI[] uris = discovery.knownUrisOf(serviceName, 1);
                    if (uris.length == 0) {
                        // //Log.info("No known URI for service " + serviceName);
                        return Result.error(Result.ErrorCode.NOT_FOUND);
                    }
                    URI uri = uris[0];
                    Result r2 = UsersClientFactory.getFeedsClient(uri).getOwnMessages(sub, time);
                    if (r2.isOK()) {
                        // //Log.info("+++++++++++++" + r2.value().toString() + "++++++++++++++");
                        List<Message> messages1 = (List<Message>) r2.value();
                        newMessages.addAll(messages1);
                    }
                } else {
                    List<Message> subMessages = userMessages.get(sub);
                    // Check if user has any messages
                    if (subMessages != null) {
                        for (Message message : subMessages) {
                            if (message.getCreationTime() > time) {
                                newMessages.add(message);
                            }
                        }
                    }
                }
            }
        }

        return Result.ok(newMessages);
    }

    @Override
    public Result<Message> getOwnMessage(String user, long mid) {
        Log.info("GetOwnMessage: user = " + user + ", mid = " + mid);

        // Check if user and time is valid
        if (user == null && mid <= 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        Result r1 = hasUser(user);
        if (!r1.isOK()) {
            // //Log.info("User not found");
            System.out.println("NAO HA USERR");
            return Result.error(r1.error());
        }

        // Check if user has the message
        List<Message> messages = userMessages.get(user);
        if (messages != null) {
            for (Message message : messages) {
                if (message.getId() == mid) {
                    // //Log.info("Message found: " + message);
                    System.out.println("JACKPOOOTTTT");
                    return Result.ok(message);
                }
            }

        }

        System.out.println("CANAAAAA");
        return Result.error(Result.ErrorCode.NOT_FOUND);
    }

    @Override
    public Result<List> getOwnMessages(String user, long time) {
        // //Log.info("GetOwnMessages: user = " + user + ", time = " + time);

        // Check if user and time is valid
        if (user == null && time <= 0) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // check if user exists
        Result r1 = hasUser(user);
        if (!r1.isOK()) {
            // //Log.info("User not found");
            return Result.error(r1.error());
        }

        List<Message> newMessages = new LinkedList<>();
        // Add their new messages
        List<Message> messages = userMessages.get(user);
        // Check if user has any messages
        if (messages != null) {
            for (Message message : messages) {
                if (message.getCreationTime() > time) {
                    newMessages.add(message);
                }
            }
        }
        // //Log.info("GETOWNMESSAGES New messages: " + newMessages.toString());
        return Result.ok(newMessages);
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        // //Log.info("Sub a User: user = " + user + ", userSub = " + userSub);
        // Check if user, the userSub and the pwd is valid
        if (user == null || userSub == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        // Check if user is valid
        Result r = isUserValid(user, pwd);
        if (!r.isOK()) {
            // //Log.info("User not found or wrong password");
            return Result.error(r.error());
        }

        // Check if userSub exists
        Result r2 = hasUser(userSub);
        if (!r2.isOK()) {
            // //Log.info("UserSub not found");
            return Result.error(r2.error());
        }

        List subs = usersSubscribed.get(user);
        // //Log.info("++++++++ " + subs + "+++++++++++++++");
        if (subs == null) {
            subs = new ArrayList();
            subs.add(userSub);
            usersSubscribed.put(user, subs);
        }
        // DOES NOT CHECK IF USER IS ALREADY SUBSCRIBED??....
        else {
            if (!subs.contains(userSub)) {
                subs.add(userSub);
            }
        }
        return Result.ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        // //Log.info("unsubscribeUser: user = " + user + ", userSub = " + userSub);
        // Check if user, the userSub and the pwd is valid
        if (user == null || userSub == null || pwd == null) {
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }
        // Check if user is valid
        Result r = isUserValid(user, pwd);
        if (!r.isOK()) {
            // //Log.info("User not found or wrong password");
            return Result.error(r.error());
        }
        // Check if userSub exists
        Result r2 = hasUser(userSub);
        if (!r2.isOK()) {
            // //Log.info("UserSub not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }
        List subs = usersSubscribed.get(user);
        if (subs == null) {
            // //Log.info("UserSub not subscribed");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        } else {
            if (subs.contains(userSub)) {
                subs.remove(userSub);
            } else {
                // //Log.info("UserSub not subscribed");
                return Result.error(Result.ErrorCode.BAD_REQUEST);
            }
        }

        return Result.ok();
    }

    @Override
    public Result<List> listSubs(String user) {

        // Check if usr is valid
        if (user == null) {
            // //Log.info("User null.");
            Result.error(Result.ErrorCode.BAD_REQUEST);
            // throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        // Check if user exists
        Result r2 = hasUser(user);
        if (!r2.isOK()) {
            // //Log.info("UserSub not found");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        List<String> subs = usersSubscribed.get(user);
        if (subs == null) {
            subs = new ArrayList<>();
        }
        return Result.ok(subs);
    }
}
