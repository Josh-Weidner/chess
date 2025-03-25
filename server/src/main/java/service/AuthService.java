package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import exception.ResponseException;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData getAuthData(String token) throws ResponseException, DataAccessException {
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

    public void deleteAuthData(String authToken) throws ResponseException, DataAccessException {
        getAuthData(authToken);
        authDAO.deleteAuth(authToken);
    }
}
