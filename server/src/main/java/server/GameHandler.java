package server;

import com.google.gson.Gson;
import service.Create.CreateRequest;
import service.Create.CreateResult;
import service.FailureResult;
import service.Join.JoinRequest;
import service.List.ListResult;
import spark.Request;
import spark.Response;
import service.GameService;
import java.util.Objects;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    private static final Gson serializer = new Gson(); // Gson instance

    public String listGames(Request req, Response res) {
        try {
            res.type("application/json");

            // translate
            String authToken = req.headers("Authorization");

            // list games
            ListResult listResult = gameService.gameList(authToken);

            res.status(200);
            return serializer.toJson(listResult);
        }
        catch (Exception e) {
            if (Objects.equals(e.getMessage(), "unauthorized")) {
                res.status(401);
            }
            else {
                res.status(500);
            }
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }

    public String createGame(Request req, Response res) {
        res.type("application/json");
        try {
            String authToken = req.headers("Authorization");

            // translate
            CreateRequest createRequest = serializer.fromJson(req.body(), CreateRequest.class);

            // check for bad request
            if (createRequest == null || createRequest.gameName() == null || createRequest.gameName().isEmpty()) {
                res.status(400);
                return serializer.toJson(new FailureResult("Error: bad request"));
            }

            // create game
            CreateResult createResult = gameService.createGame(authToken, createRequest);

            res.status(200);
            return serializer.toJson(createResult);
        }
        catch (Exception e) {
            if (Objects.equals(e.getMessage(), "unauthorized")) {
                res.status(401);
            }
            else {
                res.status(500);
            }
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }

    public String joinGame(Request req, Response res) {
        res.type("application/json");
        try {
            String authToken = req.headers("Authorization");

            // translate
            JoinRequest joinRequest = serializer.fromJson(req.body(), JoinRequest.class);

            // check bad request
            if (joinRequest == null || joinRequest.playerColor() == null || joinRequest.gameID() == null) {
                res.status(400);
                return serializer.toJson(new FailureResult("Error: bad request"));
            }

            // join game
            gameService.joinGame(authToken, joinRequest);

            res.status(200);
            return serializer.toJson("");
        }
        catch (Exception e) {
            if (Objects.equals(e.getMessage(), "unauthorized")) {
                res.status(401);
            }
            else if (Objects.equals(e.getMessage(), "already taken")) {
                res.status(403);
            }
            else {
                res.status(500);
            }
            FailureResult failureResult = new FailureResult("Error: " + e.getMessage());
            return serializer.toJson(failureResult);
        }
    }
}
