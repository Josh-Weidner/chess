package service;

import model.AuthData;
import model.UserData;
import service.Login.LoginRequest;
import service.Login.LoginResult;
import service.Register.RegisterRequest;
import service.Register.RegisterResult;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;

public class UserService {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private AuthService authService;

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
        if (userData == null) { return null; }

        // create authData
        AuthData authData = AuthService.generateAuthData(userData.username());
        AuthDAO.createAuth(authData);

        return new LoginResult(userData.username(), authData.authToken());
    }
//    public LogoutResult logout(LogoutRequest logoutRequest) {}
}
