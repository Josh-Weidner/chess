package service;

import dataaccess.GameDAO;
import model.AuthData;
import model.UserData;
import service.Login.LoginRequest;
import service.Login.LoginResult;
import service.Register.RegisterRequest;
import service.Register.RegisterResult;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;

import java.util.Objects;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final AuthService authService;

    public UserService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO, AuthService authService) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.authService = authService;
    }

    public RegisterResult register(RegisterRequest registerRequest) throws Exception {

        // check is username already exists
        UserData existingUser = userDAO.getUser(registerRequest.getUsername());
        if (existingUser != null) {
            throw new Exception("already taken");
        }

        // create user in database
        UserData newUser = new UserData(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail());
        userDAO.addUser(newUser);

        // create auth in database
        AuthData authData = AuthService.generateAuthData(newUser.username());
        authDAO.createAuth(authData);

        return new RegisterResult(newUser.username(), authData.authToken());
    }

    public LoginResult login(LoginRequest loginRequest) throws Exception {

        // get user from database
        UserData userData = userDAO.getUser(loginRequest.username());
        if (userData == null || !Objects.equals(userData.password(), loginRequest.username())) { throw new Exception("unauthorized"); }

        // create authData
        AuthData authData = AuthService.generateAuthData(userData.username());
        authDAO.createAuth(authData);

        return new LoginResult(userData.username(), authData.authToken());
    }

    public void clear(String authToken) throws Exception {
        AuthData authData = authService.getAuthData(authToken);
        if (authData == null) {
            throw new Exception("unauthorized");
        }

        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }
}
