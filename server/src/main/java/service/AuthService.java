package service;

import dataaccess.AuthDAO;
import model.AuthData;

import java.util.UUID;

public class AuthService {
    public static void clear() {
        AuthDAO.clear();
    }

    public static AuthData generateAuthData(String username) {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, username);
    }
}
