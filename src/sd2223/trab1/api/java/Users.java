package sd2223.trab1.api.java;


import sd2223.trab1.api.User;

import java.util.List;

public interface Users {

	Result<String> createUser(User user);

	Result<User> getUser(String name, String pwd);

	Result<User> updateUser(String name, String pwd, User user);

	Result<User> deleteUser(String name, String pwd);

	Result<List> searchUsers(String pattern);

	Result<Void> verifyPassword(String name, String pwd);
}
