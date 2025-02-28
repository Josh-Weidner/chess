package dataaccess;

import model.UserData;

public interface UserDAO {
    static UserData getUser(String username) {
        return null;
    }
    static void createUser(UserData user) {}
}
