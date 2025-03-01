package server;

import com.google.gson.Gson;
import service.FailureResult;
import service.Login.LoginRequest;
import service.Login.LoginResult;
import service.Register.RegisterRequest;
import service.Register.RegisterResult;
import service.UserService;
import service.AuthService;
import spark.Request;
import spark.Response;

public class UserHandler {
    private UserService userService;
    private AuthService authService;
    private static final Gson serializer = new Gson(); // Gson instance

    public String registerUser(Request req, Response res) {
        try {
            res.type("application/json");

            // get request from json
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
        res.type("application/json");

        try {
            // clear database
            authService.clear();

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
        res.type("application/json");

        try {
            // get body of request
            LoginRequest loginRequest = serializer.fromJson(req.body(), LoginRequest.class);

            // login
            LoginResult loginResult = UserService.login(loginRequest);

            // if result is null, or any fields are null it was unauthorized
            if (loginResult == null || loginResult.username() == null || loginResult.authToken() == null) {
                res.status(401);
                FailureResult failureResult = new FailureResult("Error: unauthorized");
                return serializer.toJson(failureResult);
            }

            // success
            res.status(200);
            return serializer.toJson(loginResult);
        }
        catch (Exception e) {
            res.status(500);
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }

//    public static String logoutUser(Request req, Response res) {
//        res.type("application/json");
//
//        try {
//            String authToken = req.headers("Authorization");
//        }
//    }
}