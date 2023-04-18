package sd2223.trab1.clients.soap;

import java.net.URI;
import java.util.List;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.soap.FeedsService;

import javax.xml.namespace.QName;

public class SoapFeedsClient extends SoapClient implements Feeds {

    public SoapFeedsClient(URI serverURI) {super(serverURI);}

    private FeedsService stub;

    synchronized private FeedsService stub() {
        if (stub == null) {
            QName QNAME = new QName(FeedsService.NAMESPACE, FeedsService.NAME);
            Service service = Service.create(super.toURL(super.uri + WSDL), QNAME);
            this.stub = service.getPort(FeedsService.class);
            super.setTimeouts((BindingProvider) stub);
        }
        return stub;
    }

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        return super.reTry(() -> super.toJavaResult(() -> stub().postMessage(user, pwd, msg)));
    }
    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return super.reTry(() -> super.toJavaResult(() -> stub().removeFromPersonalFeed(user, mid, pwd)));
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return super.reTry(() -> super.toJavaResult(() -> stub().getMessage(user, mid)));
    }

    @Override
    public Result<List> getMessages(String user, long time) {
        return super.reTry(() -> super.toJavaResult(() -> stub().getMessages(user, time)));


    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return super.reTry(() -> super.toJavaResult(() -> stub().subUser(user, userSub, pwd)));

    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return super.reTry(() -> super.toJavaResult(() -> stub().unsubscribeUser(user, userSub, pwd)));

    }

    @Override
    public Result<List> listSubs(String user) {
        return super.reTry(() -> super.toJavaResult(() -> stub().listSubs(user)));

    }

}
