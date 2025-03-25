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

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class Repl {
    private final Client client;

    public Repl(String serverUrl) {
        client = new Client(serverUrl, this);
    }

    public void run() {
        System.out.println();
        System.out.println(BLACK_QUEEN + "Chess 240. Type 'help' to get started." + BLACK_QUEEN);
        System.out.println();
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }
        System.out.println();
    }

    private void printPrompt() {
        System.out.print("/n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }
}