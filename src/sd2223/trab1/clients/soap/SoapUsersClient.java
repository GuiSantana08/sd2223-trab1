package sd2223.trab1.clients.soap;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.api.soap.UsersService;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

public class SoapUsersClient extends SoapClient implements Users {

    public SoapUsersClient(URI serverURI) {
        super(serverURI);
    }

    private UsersService stub;
    synchronized private UsersService stub() {
        if (stub == null) {
            QName QNAME = new QName(UsersService.NAMESPACE, UsersService.NAME);
            Service service = Service.create(super.toURL(super.uri + WSDL), QNAME);
            this.stub = service.getPort(UsersService.class);
            super.setTimeouts((BindingProvider) stub);

        }
        return stub;
    }

    @Override
    public Result<String> createUser(User user) {
        return super.reTry(() -> super.toJavaResult(() -> stub().createUser(user)));
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        return super.reTry(() -> super.toJavaResult(() -> stub().getUser(name, pwd)));
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User user) {
        return super.reTry(() -> super.toJavaResult(() -> stub().updateUser(name, pwd, user)));
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        return super.reTry(() -> super.toJavaResult(() -> stub().deleteUser(name, pwd)));
    }

    @Override
    public Result<List> searchUsers(String pattern) {
        return super.reTry(() -> super.toJavaResult(() -> stub().searchUsers(pattern)));
    }

}
