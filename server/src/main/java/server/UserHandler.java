package server;

import com.google.gson.Gson;
import service.AuthService;
import service.FailureResult;
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

    public String registerUser(Request req, Response res) {
        try {
            res.type("application/json");

            // translate
            RegisterRequest registerRequest = serializer.fromJson(req.body(), RegisterRequest.class);

            // check for bad request
            if (registerRequest.getUsername() == null || registerRequest.getPassword() == null || registerRequest.getEmail() == null) {
                res.status(400);
                FailureResult failureResult = new FailureResult("Error: bad request");
                return serializer.toJson(failureResult);
            }

            // get result from service
            RegisterResult registerResult = userService.register(registerRequest);

            // Return success response
            res.status(200);
            return serializer.toJson(registerResult);
        }
        catch (Exception e) {
            if (e.getMessage().equals("already taken")) {
                res.status(403);
            }
            else {
                res.status(500);
            }
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }

    public String clearDatabase(Request req, Response res) {
        try {
            res.type("application/json");

            // translate
            String authToken = req.headers("Authorization");

            // clear database
            userService.clear(authToken);

            // success
            res.status(200);
            return serializer.toJson("");
        }
        catch (Exception e) {
            res.status(500);
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }

    public String loginUser(Request req, Response res) {
        try {
            res.type("application/json");

            // translate
            LoginRequest loginRequest = serializer.fromJson(req.body(), LoginRequest.class);

            // login
            LoginResult loginResult = userService.login(loginRequest);

            // success
            res.status(200);
            return serializer.toJson(loginResult);
        }
        catch (Exception e) {
            if (e.getMessage().equals("unauthorized")) {
                res.status(403);
            }
            else {
                res.status(500);
            }
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }

    public String logoutUser(Request req, Response res) {
        try {
            res.type("application/json");

            // translate
            String authToken = req.headers("Authorization");

            // logout
            authService.deleteAuthData(authToken);

            res.status(200);
            return serializer.toJson("");
        }
        catch (Exception e) {
            if (e.getMessage().equals("unauthorized")) {
                res.status(401);
            }
            else {
                res.status(500);
            }
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }
}