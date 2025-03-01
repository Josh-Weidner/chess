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
            String authToken = req.headers("Authorization");

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
        try {
            String authToken = req.headers("Authorization");
            CreateRequest createRequest = serializer.fromJson(req.body(), CreateRequest.class);

            if (createRequest == null || createRequest.gameName() == null || createRequest.gameName().isEmpty()) {
                res.status(400);
                return serializer.toJson(new FailureResult("Error: bad request"));
            }

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
        try {
            String authToken = req.headers("Authorization");
            JoinRequest joinRequest = serializer.fromJson(req.body(), JoinRequest.class);

            if (joinRequest == null || joinRequest.playerColor() == null || joinRequest.gameID() == null) {
                res.status(400);
                return serializer.toJson(new FailureResult("Error: bad request"));
            }

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
