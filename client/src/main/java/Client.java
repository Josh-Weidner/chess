

import chess.ChessGame;
import com.google.gson.Gson;
import server.*;
import server.ServerFacade;
import service.create.CreateRequest;
import service.create.CreateResult;
import service.join.JoinRequest;
import service.list.GameDataModel;
import service.list.ListResult;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Client {
    private String username = null;
    private String authToken = null;
    private HashMap<Integer, GameDataModel> gameData;
    private final ServerFacade server;
    private final String serverUrl;
    private final GameHandler gameHandler;
    private final UserHandler userHandler;
    private State state = State.SIGNEDOUT;

    public Client(String serverUrl, GameHandler gameHandler, UserHandler userHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
        this.gameHandler = gameHandler;
        this.userHandler = userHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "create" -> create(params);
                case "join" -> join(params);
                case "list" -> list();
                case "observe" -> observe(params);
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private void register(String... params) throws ResponseException {
        if (params.length != 3) { throw new ResponseException(400, "Expected: <username> <password> <email>"); }

        RegisterResult registerResult = server.registerUser(new RegisterRequest(params[0], params[1], params[2]));

        username = registerResult.username();
        authToken = registerResult.authToken();

        System.out.println("Welcome " + SET_TEXT_BOLD + username + "!");
    }

    private void login(String... params) throws ResponseException {
        if (params.length != 2) { throw new ResponseException(400, "Expected: <username> <password>"); }

        LoginResult loginResult = server.loginUser(new LoginRequest(params[0], params[1]));

        username = loginResult.username();
        authToken = loginResult.authToken();

        System.out.println("Welcome " + SET_TEXT_BOLD + username + "!");
    }

    private void create(String... params) throws ResponseException {
        if (params.length != 1) { throw new ResponseException(400, "Expected: <Name>"); }

        CreateResult createResult = server.createGame(new CreateRequest(params[0]), authToken);

        System.out.println("Game " + SET_TEXT_BOLD + params[0] + " has been created!");
    }

    private void list() throws ResponseException {
        ListResult listResult = server.listGames(authToken);

        gameData = new HashMap<>();
        int gameNumber = 1;
        if (listResult.games() != null) {
            System.out.println("     Game Name:    White Username:    Black Username:    ");
            for (GameDataModel game: listResult.games()) {
                gameData.put(gameNumber, game);
                System.out.println("(" + gameNumber + ") " + game.gameName() + game.whiteUsername() + game.blackUsername());
            }
        }
        else {
            System.out.println("No games found");
        }
    }

    private void join(String... params) throws ResponseException {
        if (params.length != 2) { throw new ResponseException(400, "Expected: <Name>"); }
            ChessGame.TeamColor teamColor = ChessGame.TeamColor.valueOf(params[1]);

            GameDataModel game = ActiveGames.get(gameId);

            JoinRequest joinRequest = new JoinRequest(teamColor, game.gameID());

            serverFacade.joinGame(joinRequest, authToken);

            System.out.println("Game " + SET_TEXT_BOLD + game.gameName() + " has been joined!");
    }

    private void Observe() {
        try {
            Integer gameId = scanner.nextInt();

            GameDataModel game = ActiveGames.get(gameId);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void Logout() {
        try {
            serverFacade.logoutUser(authToken);

            registeredUsername = "";
            authToken = "";
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void DisplayCommands() {
        if (state == State.SIGNEDOUT) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    register <USERNAME> <PASSWORD> <EMAIL>" + RESET_TEXT_COLOR + " - to create an account");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - to play chess");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands");
            System.out.println();
        }
        else {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    create <NAME>" + RESET_TEXT_COLOR + " - a game");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    list" + RESET_TEXT_COLOR + " - games");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    join <ID> [WHITE/BLACK]" + RESET_TEXT_COLOR + " - a game");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    observe <ID>" + RESET_TEXT_COLOR + " - a game");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    logout" + RESET_TEXT_COLOR + " - when you are done");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands");
            System.out.println();
        }
    }

}