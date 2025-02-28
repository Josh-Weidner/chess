package service;

import model.AuthData;
import model.UserData;
import service.Login.LoginRequest;
import service.Login.LoginResult;
import service.Logout.LogoutRequest;
import service.Logout.LogoutResult;
import service.Register.RegisterRequest;
import service.Register.RegisterResult;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;

public class UserService {
    public static RegisterResult register(RegisterRequest registerRequest) {

        // check is username already exists
        UserData existingUser = UserDAO.getUser(registerRequest.getUsername());
        if (existingUser != null) { return null; }

        // create user in database
        UserData newUser = new UserData(registerRequest.getUsername(),registerRequest.getPassword(), registerRequest.getEmail());
        UserDAO.createUser(newUser);

        // create auth in database
        AuthData authData = AuthService.generateAuthData(newUser.username());
        AuthDAO.createAuth(authData);

        return new RegisterResult(newUser.username(), authData.authToken());
    }

    public static LoginResult login(LoginRequest loginRequest) {

        // get user from database
        UserData userData = UserDAO.getUser(loginRequest.username());
        if (userData == null) { return null; }

        // create authData
        AuthData authData = AuthService.generateAuthData(userData.username());
        AuthDAO.createAuth(authData);

        return new LoginResult(userData.username(), authData.authToken());
    }
//    public LogoutResult logout(LogoutRequest logoutRequest) {}
}
