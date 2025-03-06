package dataaccess.memory;

import dataaccess.UserDAO;
import model.UserData;

import java.util.HashMap;

public class UserMemoryDataAccess implements UserDAO {
    final private HashMap<String, UserData> users = new HashMap<>();

    public void addUser(UserData user) {
        users.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return users.get(username);
    }

    public void clear() {
        users.clear();
    }
}
