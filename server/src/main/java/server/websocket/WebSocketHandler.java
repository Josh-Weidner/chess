package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import exception.ResponseException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import service.WebSocketService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Objects;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();
    private final WebSocketService webSocketService;

    public WebSocketHandler(WebSocketService service) { webSocketService = service; }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getAuthToken(), command.getGameID(), session);
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove(), session);
            case RESIGN -> resign(command.getAuthToken(), command.getGameID(), session);
            case LEAVE -> leave(command.getAuthToken(), command.getGameID());
        }
    }

    private void connect(String authToken, int gameId, Session session) throws IOException {
        try {
            // Get user's name from auth Token
            String userName = webSocketService.getAuthData(authToken).username();

            // Add auth token to connections to Web socket
            connections.add(gameId, authToken, session);

            // Get game and load send a load_game notification to new connection's client
            GameData gameData = webSocketService.getGameData(gameId);
            ServerMessage serverMessageNotification;
            serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData, null, null);
            connections.broadcastToOne(gameId, authToken, serverMessageNotification);

            if (connections.connections.size() == 1) { return; }

            // Check if user is observer, white, or black and display the message depending on that
            String message;
            if (Objects.equals(gameData.whiteUsername(), userName)) {
                message = String.format("Team white, %s, has joined", userName);
            }
            else if (Objects.equals(gameData.blackUsername(), userName)) {
                message = String.format("Team black, %s, has joined", userName);
            }
            else {
                message = String.format("%s is now observing", userName);
            }
            serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcastAndExcludeOne(gameId, authToken, serverMessageNotification);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, e.getMessage(), null);
            sendError(session, serverMessageNotification);
        }
    }

    private void makeMove(String authToken, int gameId, ChessMove move, Session session) throws IOException {
        try {
            // Get user's name with auth token
            String userName = webSocketService.getAuthData(authToken).username();

            // Get game data with gameId
            GameData gameData = webSocketService.getGameData(gameId);

            // Check if game is over
            if (gameData.game().isGameOver()) {
                throw new Exception("Game is over");
            }

            // Make sure the user is a player in the game
            if (!Objects.equals(gameData.whiteUsername(), userName) && !Objects.equals(gameData.blackUsername(), userName)) {
                throw new Exception("Invalid command, user is not a player");
            }

            // Get usernames team color
            ChessGame.TeamColor teamColor = ChessGame.TeamColor.BLACK;
            if (gameData.whiteUsername().equals(userName)) {
                teamColor = ChessGame.TeamColor.WHITE;
            }

            // Make sure the piece that was passed in corresponds to the player that sent it
            ChessPiece piece = gameData.game().getBoard().getPiece(move.getStartPosition());
            if (piece == null || !piece.getTeamColor().equals(gameData.game().getTeamTurn()) || !piece.getTeamColor().equals(teamColor)) {
                throw new Exception("Invalid move, not player's turn");
            }

            // Get the game and make the move, this will check for the right team's turn and valid moves
            ChessGame game = gameData.game();
            game.makeMove(move);

            // Save the new game after the move
            webSocketService.saveGameData(gameData);

            // Update the game for everyone
            var serverMessageLoadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData, null, null);
            connections.broadcastToAll(gameId, serverMessageLoadGame);

            if (connections.connections.size() == 1) { return; }

            // Send notification to those connected through web socket
            var message = String.format("Team %s, %s, moved from %s to %s", teamColor, userName,
                    getCoordinateFromPosition(move.getStartPosition()), getCoordinateFromPosition(move.getEndPosition()));
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcastAndExcludeOne(gameId, authToken, serverMessageNotification);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, e.getMessage(), null);
            sendError(session, serverMessageNotification);
        }
    }

    private void sendError(Session session, ServerMessage serverMessageNotification) throws IOException {
        var json = new Gson().toJson(serverMessageNotification);
        session.getRemote().sendString(json);
    }

    private ChessPosition getCoordinateFromPosition(ChessPosition position) throws ResponseException {
        int first = position.getRow();
        int column = position.getColumn();
        var second = switch (column) {
            case 8 -> 'h';
            case 7 -> 'g';
            case 6 -> 'f';
            case 5 -> 'e';
            case 4 -> 'd';
            case 3 -> 'c';
            case 2 -> 'b';
            case 1 -> 'a';
            default -> throw new ResponseException(400, "Start and end position must be in form of row letter and column number. i.e. e4");
        };

        return new ChessPosition(first, second);
    }

    private void resign(String authToken, int gameId, Session session) throws IOException {
        try {
            // Get user's name with auth Token
            String userName = webSocketService.getAuthData(authToken).username();

            // Get game data with gameId
            GameData gameData = webSocketService.getGameData(gameId);

            // Check if game is already over
            if (gameData.game().isGameOver()) {
                throw new Exception("Game is over");
            }
            
            // Get team that is resigning
            String team;
            if (Objects.equals(gameData.blackUsername(), userName)) {
                team = "black";
            }
            else if (Objects.equals(gameData.whiteUsername(), userName)) {
                team = "white";
            }
            else {
                throw new Exception("Invalid command, user is not a player");
            }

            gameData.game().setGameOver();
            webSocketService.saveGameData(gameData);

            var message = String.format("Team %s, %s, has resigned", team, userName);
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcastToAll(gameId, serverMessage);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, e.getMessage(), null);
            sendError(session, serverMessageNotification);
        }
    }

    private void leave(String authToken, int gameId) throws IOException {
        try {
            // Get player and remove
            String userName = webSocketService.getAuthData(authToken).username();

            // Get game and see who is leaving
            GameData gameData = webSocketService.getGameData(gameId);

            // Check if game is over
            boolean isGameOver = gameData.game().isGameOver();

            // Depending on type of user, display message to user
            String message;
            if (Objects.equals(gameData.whiteUsername(), userName) && !isGameOver) {
                message = String.format("Team white, %s, has left the game", userName);
                GameData newGameData = gameData.withWhiteUsername(null);
                webSocketService.saveGameData(newGameData);
            }
            else if (Objects.equals(gameData.blackUsername(), userName) && !isGameOver) {
                message = String.format("Team black, %s, has left the game", userName);
                GameData newGameData = gameData.withBlackUsername(null);
                webSocketService.saveGameData(newGameData);
            }
            else {
                message = String.format("%s has stopped observing", userName);
            }
            connections.remove(gameId, authToken);
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcastToAll(gameId, serverMessage);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, e.getMessage());
            connections.broadcastToOne(gameId, authToken, serverMessageNotification);
        }
    }
}