package dataaccess;

import model.UserData;
import server.ResponseException;

public interface UserDAO {
    UserData getUser(String username) throws ResponseException;

    void addUser(UserData user) throws ResponseException;
    void clear() throws ResponseException;
}
