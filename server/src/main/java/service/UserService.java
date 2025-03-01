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
        try {
            // if name is not found, a data error will be thrown, and we will catch that and handle user creation
            userDAO.getUser(registerRequest.getUsername());
        }
        catch (Exception e) {
            // create user object and add to database
            UserData newUser = new UserData(registerRequest.getUsername(), registerRequest.getPassword(), registerRequest.getEmail());
            userDAO.addUser(newUser);

            // create auth object and add to database
            AuthData authData = AuthService.generateAuthData(newUser.username());
            authDAO.createAuth(authData);

            return new RegisterResult(newUser.username(), authData.authToken());
        }
        // if we got here it means that a user already has that username
        throw new Exception("already taken");
    }

    public LoginResult login(LoginRequest loginRequest) throws Exception {

        // get user from database
        UserData userData = userDAO.getUser(loginRequest.username());

        // verify password
        if (!Objects.equals(userData.password(), loginRequest.password())) { throw new Exception("unauthorized"); }

        // create auth object and add to database
        AuthData authData = AuthService.generateAuthData(userData.username());
        authDAO.createAuth(authData);

        return new LoginResult(userData.username(), authData.authToken());
    }

    public void clear(String authToken) throws Exception {
        // if auth is not found an exception will be thrown and no clearing will be done
        authService.getAuthData(authToken);

        authDAO.clear();
        userDAO.clear();
        gameDAO.clear();
    }
}
