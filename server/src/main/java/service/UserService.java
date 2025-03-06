package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import server.ResponseException;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import java.util.Objects;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws ResponseException {
        // bad request
        if (registerRequest.getUsername() == null || registerRequest.getPassword() == null || registerRequest.getEmail() == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        // already taken
        UserData userData = userDAO.getUser(registerRequest.getUsername());
        if (userData != null) {
            throw new ResponseException(403, "Error: already taken");
        }

        // create user object and add to database
        UserData newUser = new UserData(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail());
        userDAO.addUser(newUser);

        // create auth object and add to database
        AuthData authData = AuthService.generateAuthData(newUser.username());
        authDAO.createAuth(authData);

        return new RegisterResult(newUser.username(), authData.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws ResponseException {
        // get user from database
        UserData userData = userDAO.getUser(loginRequest.username());

        // verify password
        if (userData == null || !BCrypt.checkpw(loginRequest.password(), userData.password())) {
            throw new ResponseException(401, "Error: unauthorized");
        }

        // create auth object and add to database
        AuthData authData = AuthService.generateAuthData(userData.username());
        authDAO.createAuth(authData);

        return new LoginResult(userData.username(), authData.authToken());
    }

    public void clear() {
        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }
}
