package service;

import dataaccess.AuthDAO;
import model.AuthData;
import server.ResponseException;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData getAuthData(String token) throws ResponseException {
        AuthData authData = authDAO.getAuth(token);
        if (authData == null) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        return authData;
    }

    public static AuthData generateAuthData(String username) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, username);
    }

    public void deleteAuthData(String authToken) throws ResponseException {
        getAuthData(authToken);
        authDAO.deleteAuth(authToken);
    }
}
