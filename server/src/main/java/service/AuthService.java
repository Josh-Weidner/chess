package service;

import dataaccess.AuthDAO;
import model.AuthData;

import java.util.UUID;

public class AuthService {

    private dataaccess.AuthDAO authDAO;
    private dataaccess.UserDAO userDAO;
    private dataaccess.GameDAO gameDAO;

    public void clear() throws Exception {
        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }

    public static AuthData generateAuthData(String username) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, username);
    }
}
