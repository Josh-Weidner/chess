package service;

import dataaccess.AuthDAO;
import model.AuthData;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData getAuthData(String token) throws Exception {
        try {
            return authDAO.getAuth(token);
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public static AuthData generateAuthData(String username) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, username);
    }

    public void deleteAuthData(String authToken) throws Exception {
        getAuthData(authToken);

        authDAO.deleteAuth(authToken);
    }
}
