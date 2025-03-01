package server;

import com.google.gson.Gson;
import service.AuthService;
import service.Login.LoginRequest;
import service.Login.LoginResult;
import service.Register.RegisterRequest;
import service.Register.RegisterResult;
import service.UserService;
import spark.Request;
import spark.Response;

public class UserHandler {
    private final UserService userService;
    private final AuthService authService;

    public UserHandler(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    private static final Gson serializer = new Gson(); // Gson instance

    public String registerUser(Request req, Response res) throws ResponseException{
        res.type("application/json");

        // translate
        RegisterRequest registerRequest = serializer.fromJson(req.body(), RegisterRequest.class);
        // get result from service
        RegisterResult registerResult = userService.register(registerRequest);

        // Return success response
        res.status(200);
        return serializer.toJson(registerResult);
    }

    public String clearDatabase(Request req, Response res) {
            res.type("application/json");

            // clear database
            userService.clear();

            // success
            res.status(200);
            return "";
    }

    public String loginUser(Request req, Response res) throws ResponseException {
        res.type("application/json");

        // translate
        LoginRequest loginRequest = serializer.fromJson(req.body(), LoginRequest.class);

        // login
        LoginResult loginResult = userService.login(loginRequest);

        // success
        res.status(200);
        return serializer.toJson(loginResult);
    }

    public String logoutUser(Request req, Response res) throws ResponseException {
        res.type("application/json");

        // translate
        String authToken = req.headers("Authorization");

        // logout
        authService.deleteAuthData(authToken);

        res.status(200);
        return "";
    }
}