package client;

import client.websocket.ServerMessageHandler;
import websocket.messages.ServerMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class Repl implements ServerMessageHandler {
    private final Client client;

    public Repl(int serverUrl) {
        client = new Client(serverUrl, this);
    }

    public void run() {
        System.out.println();
        System.out.println(BLACK_QUEEN + "Chess 240. Type 'help' to get started." + BLACK_QUEEN);
        System.out.println();

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

    public void notify(ServerMessage message) {
        if (message.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            client.loadGame(message);
        }
        else if (message.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            client.notifyError(message);
        }
        else {
            client.notifyUser(message);
        }
        printPrompt();
    }

    public static void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + ">>> " + SET_TEXT_COLOR_GREEN);
    }
}