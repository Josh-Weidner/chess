import chess.ChessGame;
import com.google.gson.Gson;
import server.Server;
import service.create.CreateRequest;
import service.create.CreateResult;
import service.join.JoinRequest;
import service.list.GameDataModel;
import service.list.ListResult;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import static ui.EscapeSequences.*;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static Gson gson = new Gson();
    static Server server = new Server();
    static int port = server.run(800);
    static ServerFacade serverFacade = new ServerFacade(Integer.toString(port));
    static String registeredUsername = "";
    static String authToken = "";
    static HashMap<Integer, GameDataModel> ActiveGames;

    public static void main(String[] args) throws MalformedURLException {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);

        System.out.println();
        System.out.println(BLACK_QUEEN + "Chess 240. Type 'help' to get started." + BLACK_QUEEN);
        System.out.println();

        NextCommand();
    }

    private static void NextCommand() {
        try {
            String prompt = !registeredUsername.isEmpty() ? "[" + registeredUsername.toUpperCase() + "] >>> " : "[LOGGED_OUT] >>> ";
            System.out.print(prompt);
            String cmd = scanner.next();

            if (cmd.equalsIgnoreCase("quit")) {
                System.exit(0);
            }

            else {
                if (cmd.equalsIgnoreCase("help")) {
                    DisplayCommands();
                }
                else if (registeredUsername.isEmpty()) {
                    if (cmd.equalsIgnoreCase("register")) {
                        Register();
                    }
                    else if (cmd.equalsIgnoreCase("login")) {
                        Login();
                    }
                }
                else {
                    if (cmd.equalsIgnoreCase("create")) {
                        Create();
                    }
                    else if (cmd.equalsIgnoreCase("list")) {
                        List();
                    }
                    else if (cmd.equalsIgnoreCase("join")) {
                        Join();
                    }
                    else if (cmd.equalsIgnoreCase("observe")) {
                        Observe();
                    }
                    else if (cmd.equalsIgnoreCase("logout")) {
                        Logout();
                    }
                }
                NextCommand();
            }

        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Register() {
        try {
            String username = scanner.next();
            String password = scanner.next();
            String email = scanner.next();

            String registerRequest = new RegisterRequest(username, password, email).toString();
            String json = gson.toJson(registerRequest);

            String result = serverFacade.registerUser(json);

            RegisterResult registerResult = gson.fromJson(result, RegisterResult.class);

            registeredUsername = registerResult.username();
            authToken = registerResult.authToken();

            System.out.println("Welcome " + SET_TEXT_BOLD + registeredUsername + "!");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Login() {
        try {
            String username = scanner.next();
            String password = scanner.next();

            String loginRequest = new LoginRequest(username, password).toString();
            String json = gson.toJson(loginRequest);

            String result = serverFacade.loginUser(json);
            LoginResult loginResult = gson.fromJson(result, LoginResult.class);

            registeredUsername = loginResult.username();
            authToken = loginResult.authToken();

            System.out.println("Welcome " + SET_TEXT_BOLD + registeredUsername + "!");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Create() {
        try {
            String gameName = scanner.next();

            CreateRequest createRequest = new CreateRequest(gameName);
            String json = gson.toJson(createRequest);

            String result = serverFacade.createGame(json, authToken);
            CreateResult createResult = gson.fromJson(result, CreateResult.class);

            System.out.println("Game " + SET_TEXT_BOLD + gameName + " has been created!");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void List() {
        try {
            String result = serverFacade.listGames(authToken);

            ListResult listResult = gson.fromJson(result, ListResult.class);

            ActiveGames = new HashMap<>();
            int gameNumber = 1;
            if (listResult.games() != null) {
                System.out.println("     Game Name:    White Username:    Black Username:    ");
                for (GameDataModel game: listResult.games()) {
                    ActiveGames.put(gameNumber, game);
                    System.out.println("(" + gameNumber + ") " + game.gameName() + game.whiteUsername() + game.blackUsername());
                }
            }
            else {
                System.out.println("No games found");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Join() {
        try {
            Integer gameId = scanner.nextInt();
            String color = scanner.next();
            ChessGame.TeamColor teamColor = ChessGame.TeamColor.valueOf(color);

            GameDataModel game = ActiveGames.get(gameId);

            JoinRequest joinRequest = new JoinRequest(teamColor, game.gameID());

            String json = gson.toJson(joinRequest);

            serverFacade.joinGame(json, authToken);

            System.out.println("Game " + SET_TEXT_BOLD + game.gameName() + " has been joined!");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Observe() {
        try {
            Integer gameId = scanner.nextInt();

            GameDataModel game = ActiveGames.get(gameId);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void Logout() {
        try {
            serverFacade.logoutUser(authToken);

            registeredUsername = "";
            authToken = "";
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void DisplayCommands() {
        if (registeredUsername.isEmpty()) {
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