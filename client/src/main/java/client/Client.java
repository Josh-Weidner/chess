package client;

import chess.ChessGame;
import server.*;
import service.create.CreateRequest;
import service.join.JoinRequest;
import service.list.GameDataModel;
import service.list.ListResult;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import java.util.Arrays;
import java.util.HashMap;

import static ui.EscapeSequences.*;

public class Client {
    private String username = null;
    private String authToken = null;
    private HashMap<Integer, GameDataModel> gameData;
    private final ServerFacade server;
    private boolean isLoggedIn = false;

    public Client(int serverUrl) {
        server = new ServerFacade(serverUrl);
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
                case "logout" -> logout();
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String register(String... params) throws ResponseException {
        if (params.length != 3) { throw new ResponseException(400, "Expected: <username> <password> <email>"); }

        RegisterResult registerResult = server.registerUser(new RegisterRequest(params[0], params[1], params[2]));

        username = registerResult.username();
        authToken = registerResult.authToken();
        isLoggedIn = true;

        return String.format("Welcome " + SET_TEXT_BOLD + username + "!");
    }

    private String login(String... params) throws ResponseException {
        if (params.length != 2) { throw new ResponseException(400, "Expected: <username> <password>"); }

        LoginResult loginResult = server.loginUser(new LoginRequest(params[0], params[1]));

        username = loginResult.username();
        authToken = loginResult.authToken();
        isLoggedIn = true;

        return String.format("Welcome " + SET_TEXT_BOLD + username + "!");
    }

    private String create(String... params) throws ResponseException {
        if (params.length != 1) { throw new ResponseException(400, "Expected: <Name>"); }

        server.createGame(new CreateRequest(params[0]), authToken);

        return String.format("Game " + SET_TEXT_BOLD + params[0] + " has been created!");
    }

    private String list() throws ResponseException {
        ListResult listResult = server.listGames(authToken);

        gameData = new HashMap<>();
        if (listResult.games() != null) {
            StringBuilder gameList = new StringBuilder();
            gameList.append("     Game Name:    White Username:    Black Username:    \n");
            int gameNumber = 1;
            for (GameDataModel game: listResult.games()) {
                gameData.put(gameNumber, game);
                gameList.append("(").append(gameNumber).append(") ").append(game.gameName()).append(game.whiteUsername()).append(game.blackUsername());
            }
            return gameList.toString();
        }
        else {
            return "No games found";
        }
    }

    private String join(String... params) throws ResponseException {
        if (params.length != 2) { throw new ResponseException(400, "Expected: <Name>"); }

        int gameId = getGameId(params[0]);

        if (!params[1].equals("WHITE") && !params[1].equals("BLACK")) { throw new ResponseException(400, "Invalid team color!"); }

        ChessGame.TeamColor teamColor = ChessGame.TeamColor.valueOf(params[1]);
        GameDataModel game = gameData.get(gameId);
        if (game == null) {
            throw new ResponseException(400, "Game does not exist!");
        }

        server.joinGame(new JoinRequest(teamColor, game.gameID()), authToken);

        return String.format("You have joined the game: " + SET_TEXT_BOLD + game.gameName());
    }

    private String observe(String... params) throws ResponseException {
        if (params.length != 1) { throw new ResponseException(400, "Expected: <Name>"); }

        int gameId = getGameId(params[0]);

        GameDataModel game = gameData.get(gameId);
        if (game == null) {
            throw new ResponseException(400, "Game does not exist!");
        }

        return String.format("You are now observing the game: " + SET_TEXT_BOLD + game.gameName());
    }

    private int getGameId(String gameString) throws ResponseException {
        int gameId;
        try {
            gameId = Integer.parseInt(gameString);
            return gameId;
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Invalid gameId: " + gameString);
        }
    }

    private String logout() throws ResponseException {
        server.logoutUser(authToken);

        username = "";
        authToken = "";
        isLoggedIn = false;

        return "Successfully logged out!";
    }

    public String help() {
        StringBuilder builder = new StringBuilder();
        if (!isLoggedIn) {
            builder.append(SET_TEXT_COLOR_MAGENTA + "    register <USERNAME> <PASSWORD> <EMAIL>" + RESET_TEXT_COLOR + " - to create an account \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - to play chess \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands \n");
        }
        else {
            builder.append(SET_TEXT_COLOR_MAGENTA + "    create <NAME>" + RESET_TEXT_COLOR + " - a game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    list" + RESET_TEXT_COLOR + " - games \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    join <ID> [WHITE/BLACK]" + RESET_TEXT_COLOR + " - a game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    observe <ID>" + RESET_TEXT_COLOR + " - a game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    logout" + RESET_TEXT_COLOR + " - when you are done \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands \n");
        }
        return builder.toString();
    }

}