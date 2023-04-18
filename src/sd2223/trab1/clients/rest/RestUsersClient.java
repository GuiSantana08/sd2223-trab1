package trabalho1.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import trabalho1.api.User;
import trabalho1.api.java.Result;
import trabalho1.api.java.Users;
import trabalho1.api.rest.UsersService;

import java.net.URI;
import java.util.List;

public class RestUsersClient extends RestClient implements Users {

    final WebTarget target;

    public RestUsersClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(UsersService.PATH);
    }

    private Result<String> clt_createUser(User user) {
        Response r = target.request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        return super.toJavaResult(r, String.class);
    }

    private Result<User> clt_getUser(String name, String pwd) {
        Response r = target.path(name)
                .queryParam(UsersService.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        return super.toJavaResult(r, User.class);
    }

    private Result<Void> clt_verifyPassword(String name, String pwd) {
        Response r = target.path(name).path(UsersService.PWD)
                .queryParam(UsersService.PWD, pwd).request()
                .get();

        return super.toJavaResult(r, Void.class);
    }

    private Result<User> clt_updateUser(String name, String pwd, User user) {
        Response r = target.path(name)
                .queryParam(UsersService.PWD, pwd).request().accept(MediaType.APPLICATION_JSON)
                .post(Entity.entity(user, MediaType.APPLICATION_JSON));
        if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, User.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());
        return null;
    }

    private Result<User> clt_deleteUser(String name, String pwd) {
        Response r = target.path(name).queryParam(UsersService.PWD, pwd).request().accept(MediaType.APPLICATION_JSON)
                .delete();
        if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, User.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());
        return null;
    }

    private Result<List> clt_searchUsers(String pattern) {
        Response r = target.path(UsersService.PATH)
                .queryParam(UsersService.QUERY, pattern).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if (r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity())
            return super.toJavaResult(r, List.class);
        else
            System.out.println("Error, HTTP error status: " + r.getStatus());
        return null;
    }

    @Override
    public Result<String> createUser(User user) {
        return super.reTry(() -> clt_createUser(user));
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        return super.reTry(() -> clt_getUser(name, pwd));
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User user) {
        return super.reTry(() -> clt_updateUser(name, pwd, user));
    }

    @Override
    public Result<User> deleteUser(String name, String pwd) {
        return super.reTry(() -> clt_deleteUser(name, pwd));
    }

    @Override
    public Result<List> searchUsers(String pattern) {
        return super.reTry(() -> clt_searchUsers(pattern));
    }

    @Override
    public Result<Void> verifyPassword(String name, String pwd) {
        return super.reTry(() -> clt_verifyPassword(name, pwd));
    }
}
