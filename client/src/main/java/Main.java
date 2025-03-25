// import chess.*;

import static ui.EscapeSequences.*;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
//        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
//        System.out.println("♕ 240 Chess Client: " + piece);

        System.out.println();
        System.out.println(BLACK_QUEEN + "Welcome to 240 Chess. Type 'ready' to get started." + BLACK_QUEEN);
        System.out.println();

        System.out.print("[LOGGED_OUT] >>> ");
        String input = scanner.nextLine();

        if (input.equalsIgnoreCase("ready")) {
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    register <USERNAME> <PASSWORD> <EMAIL>" + RESET_TEXT_COLOR + " - to create an account");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - to play chess");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program");
            System.out.println(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands");
            System.out.println();
        }

        System.out.print("[LOGGED_OUT] >>> ");
        String input2 = scanner.nextLine();

        if (input2.equalsIgnoreCase("register")) {

        }
    }
}