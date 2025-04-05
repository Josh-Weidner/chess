package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
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
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove());
            case RESIGN -> resign(command.getAuthToken());
            case LEAVE -> leave(command.getAuthToken(), command.getGameID());
        }
    }

    private void connect(String authToken, int gameId, Session session) throws IOException {
        try {
            // Get user's name from auth Token
            String userName = webSocketService.getAuthData(authToken).username();

            // Add auth token to connections to Web socket
            connections.add(authToken, session);

            // Get game and load send a load_game notification to new connection's client
            GameData gameData = webSocketService.getGameData(gameId);
            ServerMessage serverMessageNotification;
            serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game(), null, null);
            connections.broadcast(authToken, serverMessageNotification);

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
            connections.broadcast(authToken, serverMessageNotification);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, e.getMessage());
            connections.broadcast(authToken, serverMessageNotification);
        }
    }

    private void makeMove(String authToken, int gameId, ChessMove move) throws IOException {
        try {
            // Get user's name with auth token
            String userName = webSocketService.getAuthData(authToken).username();

            // Get game data with gameId
            GameData gameData = webSocketService.getGameData(gameId);

            // Make sure the user is a player in the game
            if (!Objects.equals(gameData.whiteUsername(), userName) && !Objects.equals(gameData.blackUsername(), userName)) {
                throw new Exception("Invalid command, user is not a player");
            }

            // Get the game and make the move, this will check for the right team's turn and valid moves
            ChessGame game = gameData.game();
            game.makeMove(move);

            // Save the new game after the move
            webSocketService.saveGameData(gameData);

            // Send notification to those connected through web socket
            var message = String.format("%s made a move", userName);
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcast(authToken, serverMessageNotification);

            // Update the game for everyone
            var serverMessageLoadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game, null, null);
            connections.broadcast(authToken, serverMessageLoadGame);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, e.getMessage());
            connections.broadcast(authToken, serverMessageNotification);
        }
    }

    private void resign(String authToken) throws IOException {
        // Get player

        // don't change team turn so the opponent cant move or make it null????

        var message = String.format("%s has resigned", authToken);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
        connections.broadcast(authToken, serverMessage);
    }

    private void leave(String authToken, int gameId) throws IOException {
        // Get player and remove

        // Get game and see who is leaving

        connections.remove(authToken);

        // If observer, white, or black display the message
        var message = String.format("%s left the game", authToken);
        var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
        connections.broadcast(authToken, serverMessage);
    }
}