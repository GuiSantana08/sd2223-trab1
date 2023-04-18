package trabalho1.api.java;

import java.util.List;

import trabalho1.api.Message;

public interface Feeds {

    Result<Long> postMessage(String user, String pwd, Message msg);

    Result<Void> removeFromPersonalFeed(String user, long mid, String pwd);

    Result<Message> getMessage(String user, long mid);

    Result<List> getMessages(String user, long time);

    Result<Void> subUser(String user, String userSub, String pwd);

    Result<Void> unsubscribeUser(String user, String userSub, String pwd);

    Result<List> listSubs(String user);
}
