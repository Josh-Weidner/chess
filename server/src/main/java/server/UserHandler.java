package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.AuthService;
import model.login.LoginRequest;
import model.login.LoginResult;
import model.register.RegisterRequest;
import model.register.RegisterResult;
import service.UserService;
import spark.Request;
import spark.Response;
import exception.ResponseException;

public class UserHandler {
    private final UserService userService;
    private final AuthService authService;

    public UserHandler(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    private static final Gson SERIALIZER = new Gson(); // Gson instance

    public String registerUser(Request req, Response res) throws ResponseException, DataAccessException {
        res.type("application/json");

        // translate
        RegisterRequest registerRequest = SERIALIZER.fromJson(req.body(), RegisterRequest.class);
        // get result from service
        RegisterResult registerResult = userService.register(registerRequest);

        // Return success response
        res.status(200);
        return SERIALIZER.toJson(registerResult);
    }

    public String clearDatabase(Request req, Response res) throws DataAccessException {
            res.type("application/json");

            // clear database
            userService.clear();

            // success
            res.status(200);
            return "";
    }

    public String loginUser(Request req, Response res) throws ResponseException, DataAccessException {
        res.type("application/json");

        // translate
        LoginRequest loginRequest = SERIALIZER.fromJson(req.body(), LoginRequest.class);

        // login
        LoginResult loginResult = userService.login(loginRequest);

        // success
        res.status(200);
        return SERIALIZER.toJson(loginResult);
    }

    public String logoutUser(Request req, Response res) throws ResponseException, DataAccessException {
        res.type("application/json");

        // translate
        String authToken = req.headers("Authorization");

        // logout
        authService.deleteAuthData(authToken);

        res.status(200);
        return "";
    }
}