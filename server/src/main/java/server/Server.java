package server;

import dataaccess.*;
import dataaccess.memory.AuthMemoryDataAccess;
import dataaccess.memory.GameMemoryDataAccess;
import dataaccess.memory.UserMemoryDataAccess;
import service.AuthService;
import service.GameService;
import service.UserService;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        UserDAO userDAO = new UserMemoryDataAccess();
        GameDAO gameDAO = new GameMemoryDataAccess();
        AuthDAO authDAO = new AuthMemoryDataAccess();

        if (args.length >= 2 && args[1].equals("sql")) {
            dataAccess = new MySqlDataAccess();
        }

        UserService userService = new UserService (userDAO, authDAO, gameDAO);
        AuthService authService = new AuthService(authDAO);
        GameService gameService = new GameService(gameDAO, authService);

        // Register your endpoints and handle exceptions here.
        UserHandler  userHandler = new UserHandler(userService, authService);
        Spark.post("/user", userHandler::registerUser);
        Spark.delete("/db", userHandler::clearDatabase);
        Spark.post("/session", userHandler::loginUser);
        Spark.delete("/session", userHandler::logoutUser);

        GameHandler gameHandler = new GameHandler(gameService);
        Spark.get("/game", gameHandler::listGames);
        Spark.post("/game", gameHandler::createGame);
        Spark.put("/game", gameHandler::joinGame);

        Spark.exception(ResponseException.class, this::exceptionHandler);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private void exceptionHandler(ResponseException ex, Request req, Response res) {
        res.status(ex.statusCode());
        res.body(ex.toJson());
    }
}
