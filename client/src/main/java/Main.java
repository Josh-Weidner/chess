import com.google.gson.Gson;
import server.Server;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import static ui.EscapeSequences.*;

import java.net.MalformedURLException;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static Gson gson = new Gson();
    Server server = new Server();
    static int port = server.run(800);
    static ServerFacade serverFacade = new ServerFacade(Integer.toString(port));
    static String registeredUsername = "";

    public static void main(String[] args) throws MalformedURLException {
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);

        System.out.println();
        System.out.println(BLACK_QUEEN + "Chess 240. Type 'help' to get started." + BLACK_QUEEN);
        System.out.println();

        NextCommand(registeredUsername);
    }

    private static void NextCommand(String username) {
        String prompt = !username.isEmpty() ? "[" + username.toUpperCase() + "] >>> " : "[LOGGED_OUT] >>> ";
        System.out.print(prompt);
        String cmd = scanner.next();

        if (prompt.equals("[LOGGED_OUT] >>> ")) {
            if (cmd.equalsIgnoreCase("register")) {
                Register();
            }
            else if (cmd.equalsIgnoreCase("login")) {
                Login();
            }
            else if (cmd.equalsIgnoreCase("quit")) {
                Quit();
            }
            else if (cmd.equalsIgnoreCase("help")) {
                DisplayCommands(username);
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
            else if (cmd.equalsIgnoreCase("quit")) {
                Quit();
            }
            else if (cmd.equalsIgnoreCase("help")) {
                DisplayCommands(username);
            }
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

            System.out.println("Welcome " + SET_TEXT_BOLD + registeredUsername + "!");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: if not enough arguments ask again
            // TODO: if cant register user, prompt for new username
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
            System.out.println("Welcome " + SET_TEXT_BOLD + registeredUsername + "!");
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: request valid credentials
        }
    }

    private static void DisplayCommands(String username) {
        if (username.isEmpty()) {
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