import chess.*;

import com.google.gson.Gson;
import server.Server;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import static ui.EscapeSequences.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws MalformedURLException {
        Scanner scanner = new Scanner(System.in);
        Gson gson = new Gson();
        Server server = new Server();
        int port = server.run(800);
        ServerFacade serverFacade = new ServerFacade(Integer.toString(port));
        String registeredUsername = "";
        String prompt = !registeredUsername.equals("") ? "[" + registeredUsername.toUpperCase() + "] >>> " : "[LOGGED_OUT] >>> ";
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);

        System.out.println();
        System.out.println(BLACK_QUEEN + "Chess 240. Type 'ready' to get started." + BLACK_QUEEN);
        System.out.println();

        System.out.print(prompt);
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("ready")) {
            displayCommands(registeredUsername);
        }

        System.out.print(prompt);
        String cmd = scanner.next();

        if (cmd.equalsIgnoreCase("register")) {
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

        else if (cmd.equalsIgnoreCase("login")) {
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

        else if (cmd.equalsIgnoreCase("quit")) {
            System.exit(0);
        }

        else if (cmd.equalsIgnoreCase("help")) {
            displayCommands(registeredUsername);
        }
    }

    private static void displayCommands(String username) {
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