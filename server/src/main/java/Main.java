import chess.*;
import dataaccess.UserDAO;
import dataaccess.UserMemoryDataAccess;
import server.Server;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
        Server server = new Server();
        server.run(8080);
        UserDAO userDAO = new UserMemoryDataAccess();
    }
}