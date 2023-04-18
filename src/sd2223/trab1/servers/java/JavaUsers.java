package trabalho1.servers.java;

import jakarta.ws.rs.WebApplicationException;
import trabalho1.api.User;
import trabalho1.api.java.Result;
import trabalho1.api.java.Users;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.Checksum;

import jakarta.ws.rs.core.Response;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());
    private final Map<String, User> users = new HashMap<>();


    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        //Check if user data is valid
        if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
            Log.info("User object invalid.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        //Insert user, checking if name already exists
        if(users.putIfAbsent(user.getName(), user) != null) {
            Log.info("User already exists.");
            return Result.error(Result.ErrorCode.CONFLICT);
        }
        return Result.ok(user.getName());
    }
    @Override
    public Result<User> getUser(String name, String pwd) {
        Log.info("getUser : user = " + name + "; pwd = " + pwd);

        //Check if user is valid
        if(name == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
        }

        User user = users.get(name);
        //Check if user exists
        if(user == null) {
            Log.info("User does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
        }

        //Check if the password is correct
        if(!user.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(Result.ErrorCode.FORBIDDEN);
        }
        return Result.ok(user);
    }

    @Override
    public Result<User> updateUser(String name, String pwd, User user) {
        //return Result.error(Result.ErrorCode.NOT_IMPLEMENTED);
        Log.info("updateUser : user = " + name + "; pwd = " + pwd);

        if(name == null || pwd == null) {
            Log.info("UserId or password or user null");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
            //throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }

        User user1 = users.get(name);

        //Check if user exists
        if(user1 == null) {
            Log.info("User does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
            //throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        //Check if the password is correct
        if(user1.getPwd().equals(pwd)) {
            users.put(name, user);
            Log.info("User updated.");
        }else {
            Log.info("Password is incorrect.");
            return Result.error(Result.ErrorCode.FORBIDDEN);
            //throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return Result.ok(user);
    }


    @Override
    public Result<User> deleteUser(String name, String pwd) {
        Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
        //Check if user is valid
        if(name == null || pwd == null) {
            Log.info("UserId or Password null.");
            return Result.error(Result.ErrorCode.BAD_REQUEST);
            //throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        User user = users.get(name);

        //Check if user exists
        if(user == null) {
            Log.info("User does not exist.");
            return Result.error(Result.ErrorCode.NOT_FOUND);
            //throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        //Check if the password is correct
        if(!user.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            return Result.error(Result.ErrorCode.FORBIDDEN);
            //throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
            users.remove(name);
            Log.info("User deleted.");
            return Result.ok(user);
    }

    @Override
    public Result<List> searchUsers(String pattern) {
        List<User> list =  new LinkedList<User>();
        Log.info("searchUsers : pattern = " + pattern);

        users.forEach((x, temp) ->{
            if(temp.getDisplayName().contains(pattern))
                list.add(temp);
        });
        return Result.ok(list);
    }

    @Override
    public Result<Void> verifyPassword(String name, String pwd) {
        Result<User> result = getUser(name, pwd);
        if(result.isOK())
            return Result.ok();
        else
            return Result.error(result.error());
    }
}
