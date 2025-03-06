package dataaccess;

import model.AuthData;
import server.ResponseException;

public interface AuthDAO {
    void clear() throws ResponseException;
    void createAuth(AuthData authData) throws ResponseException;
    AuthData getAuth(String authToken) throws ResponseException;
    void deleteAuth(String authToken) throws ResponseException;
}
