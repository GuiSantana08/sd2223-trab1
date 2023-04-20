package sd2223.trab1.api.java;

import sd2223.trab1.api.Message;

import java.util.List;

public interface Feeds {

    Result<Long> postMessage(String user, String pwd, Message msg);

    Result<Void> removeFromPersonalFeed(String user, long mid, String pwd);

    Result<Message> getMessage(String user, long mid);

    Result<List> getMessages(String user, long time);

    Result<List> getOwnMessages(String user, long time);

    Result<Void> subUser(String user, String userSub, String pwd);

    Result<Void> unsubscribeUser(String user, String userSub, String pwd);

    Result<List> listSubs(String user);
}
