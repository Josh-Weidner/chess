package service;

import dataaccess.AuthDAO;
import model.AuthData;

import java.util.UUID;

public class AuthService {
    private final AuthDAO authDAO;

    public AuthService(AuthDAO authDAO) {
        this.authDAO = authDAO;
    }

    public AuthData getAuthData(String token){
        return authDAO.getAuth(token);
    }

    public static AuthData generateAuthData(String username) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, username);
    }

    public void deleteAuthData(String authToken) throws Exception {
        AuthData authData = getAuthData(authToken);
        if (authData == null) {
            throw new Exception("unauthorized");
        }
        authDAO.deleteAuth(authToken);
    }
}
