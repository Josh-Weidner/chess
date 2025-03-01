package server;

import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        UserHandler userHandler = new UserHandler();
        Spark.post("/user", userHandler::registerUser);
        Spark.delete("/db", userHandler::clearDatabase);
        Spark.post("/session", userHandler::loginUser);
        Spark.delete("/session", UserHandler::logoutUser);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
