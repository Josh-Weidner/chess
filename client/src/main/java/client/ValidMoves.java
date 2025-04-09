package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.Collection;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.RESET_BG_COLOR;

public class ValidMoves {

    public static String buildBoardWithValidMoves(GameData game, ChessPosition position, Collection<ChessMove> moves, String username) {
        StringBuilder board = new StringBuilder();
        board.append(RESET_TEXT_COLOR);

        ChessPiece[][] matrix = game.game().getBoard().chessBoard;

        ChessGame.TeamColor team = Client.getTeam(game, username);

        if (team == ChessGame.TeamColor.BLACK) {
            buildBlackBoardWithValidMoves(board, position, moves, matrix);
        }
        else {
            buildWhiteBoardWithValidMoves(board, position, moves, matrix);
        }
        return board.toString();
    }

    private static void buildBlackBoardWithValidMoves(StringBuilder board, ChessPosition position,
                                                      Collection<ChessMove> moves, ChessPiece[][] matrix) {
        board.append(SET_BG_COLOR_MAGENTA
                + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   "
                + RESET_BG_COLOR + "\n");

        int rows = matrix.length;
        int cols = matrix[0].length;
        int rowNum = 0;
        for (int i = 0; i < rows; i++) {
            rowNum = rowNum + 1;
            board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR);
            for (int j = 0; j < cols; j++) {
                ChessPiece chessPiece = matrix[i][7 - j];
                String pieceString = Client.getPieceString(chessPiece);
                ChessPosition newPosition = new ChessPosition(i+1, 8-j);
                getSquareWithValidMoves(newPosition, position, board, pieceString, i, j, moves);
            }
            board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR).append("\n");
        }

        board.append(SET_BG_COLOR_MAGENTA
                + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   "
                + RESET_BG_COLOR + "\n");
    }

    private static void buildWhiteBoardWithValidMoves(StringBuilder board, ChessPosition position, Collection<ChessMove> moves, ChessPiece[][] matrix)
    {
        board.append(SET_BG_COLOR_MAGENTA
                + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   "
                + RESET_BG_COLOR + "\n");

        int rows = matrix.length;
        int cols = matrix[0].length;
        int rowNum = 9;
        for (int i = 0; i < rows; i++) {
            rowNum = rowNum - 1;
            board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR);
            for (int j = 0; j < cols; j++) {
                ChessPiece chessPiece = matrix[7 - i][j];
                String pieceString = Client.getPieceString(chessPiece);
                ChessPosition newPosition = new ChessPosition(8 - i, j+1);
                getSquareWithValidMoves(newPosition, position, board, pieceString, i, j, moves);
            }
            board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR).append("\n");
        }

        board.append(SET_BG_COLOR_MAGENTA
                + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   "
                + RESET_BG_COLOR + "\n");
    }

    private static void getSquareWithValidMoves(ChessPosition newPosition, ChessPosition position, StringBuilder board,
                                         String pieceString, int i, int j, Collection<ChessMove> moves) {
        if (newPosition.equals(position)) {
            board.append(SET_BG_COLOR_YELLOW).append(pieceString).append(RESET_BG_COLOR);
            return;
        }
        if ((i + j) % 2 == 0) {
            getSquareColorWithValidMoves(newPosition, board, pieceString, moves, SET_BG_COLOR_GREEN, SET_BG_COLOR_WHITE);
        } else {
            getSquareColorWithValidMoves(newPosition, board, pieceString, moves, SET_BG_COLOR_DARK_GREEN, SET_BG_COLOR_BLACK);
        }
    }

    private static void getSquareColorWithValidMoves(ChessPosition newPosition, StringBuilder board,
                                              String pieceString, Collection<ChessMove> moves, String color, String color1) {
        boolean matched = false;
        for (ChessMove move : moves) {
            if (newPosition.equals(move.getEndPosition())) {
                board.append(color).append(pieceString).append(RESET_BG_COLOR);
                matched = true;
                break;
            }
        }
        if (!matched) {
            board.append(color1).append(pieceString).append(RESET_BG_COLOR);
        }
    }
}
