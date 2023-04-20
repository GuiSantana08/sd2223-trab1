package sd2223.trab1.clients.rest;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.servers.java.JavaFeeds;

import java.util.logging.Logger;

public class RestFeedsClient extends RestClient implements Feeds {

    private static final String DIR = "/";
    private static final String SUB = "/sub";

    private static Logger Log;

    final WebTarget target;

    public RestFeedsClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(FeedsService.PATH);
        Log = Logger.getLogger(JavaFeeds.class.getName());
    }

    private Result<Long> clt_postMessage(String user, String pwd, Message msg) {
        String mid = String.valueOf(System.currentTimeMillis());
        Response r = target.path(user + DIR + mid)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(msg, MediaType.APPLICATION_JSON));
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, Long.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());
        return null;
    }

    public Result<Void> clt_removeFromPersonalFeed(String user, long mid, String pwd) {
        String midString = Long.toString(mid);
        Response r = target.path(user + DIR + midString)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .delete();
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, void.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());

        return null;
    }

    public Result<Message> clt_getMessage(String user, long mid) {
        String midString = Long.toString(mid);
        Response r = target.path(user + "/" + midString)
                .queryParam(FeedsService.MID, mid)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, Message.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());

        return null;
    }

    public Result<List> clt_getMessages(String user, long time) {
        Response r = target.path(user)
                .queryParam(FeedsService.TIME, time)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()) {
            List<Message> messages = r.readEntity(new GenericType<List<Message>>(){});
            List<Message> filtered = messages.stream()
                    .filter(m -> m.getId() < time)
                    .collect(Collectors.toList());
            return Result.ok(filtered);
        }else {
            System.out.println("Error, HTTP error status: " + r.getStatus());
        }

        return null;
    }

    public Result<List> clt_getOwnMessages(String user, long time) {;
        Response r = target
                .path("own")
                .path(user)
                .queryParam(FeedsService.TIME, time)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        Log.info(r.toString());
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity()) {
            List<Message> messages = r.readEntity(new GenericType<List<Message>>(){});
            return Result.ok(messages);
        }else {
            System.out.println("Error, HTTP error status: " + r.getStatus());
        }
        return null;
    }

    private Result<Void> clt_subUser(String user, String userSub, String pwd) {
        Response r = target.path(user + DIR + SUB + DIR + userSub)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON).post(Entity.entity(null, MediaType.APPLICATION_JSON));
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, void.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());
        return null;

    }

    private Result<Void> clt_unsubscribeUser(String user, String userSub, String pwd) {
        Response r = target.path(user + DIR + SUB + DIR + userSub)
                .queryParam(FeedsService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON).delete();
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, void.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());
        return null;
    }

    private Result<List> clt_listSubs(String user) {
        Response r = target.path(user + DIR + SUB)
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        if(r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, List.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());
        return null;
    }
    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        return  super.reTry(() -> clt_postMessage(user, pwd, msg));
    }
    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return  super.reTry(() -> clt_removeFromPersonalFeed(user, mid, pwd));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return super.reTry(() -> clt_getMessage(user, mid));
    }

    @Override
    public Result<List> getMessages(String user, long time) {
        return super.reTry(() -> clt_getMessages(user, time));
    }

    @Override
    public Result<List> getOwnMessages(String user, long time) {
        return super.reTry(() -> clt_getOwnMessages(user, time));
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return super.reTry(() -> clt_subUser(user, userSub, pwd));
    }


    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return super.reTry(() -> clt_unsubscribeUser(user, userSub, pwd));
    }

    @Override
    public Result<List> listSubs(String user) {
        return super.reTry(() -> clt_listSubs(user));

    }


}
