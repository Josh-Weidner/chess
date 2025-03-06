package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import service.create.CreateRequest;
import service.create.CreateResult;
import service.join.JoinRequest;
import service.list.ListResult;
import spark.Request;
import spark.Response;
import service.GameService;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    private static final Gson SERIALIZER = new Gson(); // Gson instance

    public String listGames(Request req, Response res) throws ResponseException, DataAccessException {
        res.type("application/json");

        // translate
        String authToken = req.headers("Authorization");

        // list games
        ListResult listResult = gameService.gameList(authToken);

        res.status(200);
        return SERIALIZER.toJson(listResult);
    }

    public String createGame(Request req, Response res) throws ResponseException, DataAccessException {
        res.type("application/json");
        String authToken = req.headers("Authorization");

        // translate
        CreateRequest createRequest = SERIALIZER.fromJson(req.body(), CreateRequest.class);

        // create game
        CreateResult createResult = gameService.createGame(authToken, createRequest);

        res.status(200);
        return SERIALIZER.toJson(createResult);
    }

    public String joinGame(Request req, Response res) throws ResponseException, DataAccessException {
        res.type("application/json");
        String authToken = req.headers("Authorization");

        // translate
        JoinRequest joinRequest = SERIALIZER.fromJson(req.body(), JoinRequest.class);

        // join game
        gameService.joinGame(authToken, joinRequest);

        res.status(200);
        return "";
    }
}
