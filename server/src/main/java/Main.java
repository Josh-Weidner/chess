import chess.*;
import dataaccess.*;
import server.Server;
import service.AuthService;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        UserDAO userDAO = new UserMemoryDataAccess();
        GameDAO gameDAO = new GameMemoryDataAccess();
        AuthDAO authDAO = new AuthMemoryDataAccess();

        AuthService authService = new AuthService(authDAO);
        UserService userService = new UserService(userDAO, authDAO, gameDAO, authService);
        GameService gameService = new GameService(gameDAO, authService);

        Server server = new Server(userService, gameService, authService);
        server.run(8080);
    }
}