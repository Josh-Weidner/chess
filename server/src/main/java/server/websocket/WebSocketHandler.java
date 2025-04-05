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
            case RESIGN -> resign(command.getAuthToken(), command.getGameID());
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
            serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData, null, null);
            connections.broadcastToOne(authToken, serverMessageNotification);

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
            connections.broadcastToAll(serverMessageNotification);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, e.getMessage());
            connections.broadcastToOne(authToken, serverMessageNotification);
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

            // Update the game for everyone
            var serverMessageLoadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData, null, null);
            connections.broadcastToAll(serverMessageLoadGame);

            // Send notification to those connected through web socket
            var message = String.format("%s made a move", userName);
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcastToAll(serverMessageNotification);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, e.getMessage());
            connections.broadcastToOne(authToken, serverMessageNotification);
        }
    }

    private void resign(String authToken, int gameId) throws IOException {
        try {
            // Get user's name with auth Token
            String userName = webSocketService.getAuthData(authToken).username();

            // Get game data with gameId
            GameData gameData = webSocketService.getGameData(gameId);
            
            // Get team that is resigning
            String team;
            if (!Objects.equals(gameData.blackUsername(), userName)) {
                team = "black";
            }
            else if (!Objects.equals(gameData.whiteUsername(), userName)) {
                team = "white";
            }
            else {
                throw new Exception("Invalid command, user is not a player");
            }

            var message = String.format("Team %s, %s, has resigned", team, userName);
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcastToAll(serverMessage);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, e.getMessage());
            connections.broadcastToOne(authToken, serverMessageNotification);
        }
    }

    private void leave(String authToken, int gameId) throws IOException {
        try {
            // Get player and remove
            String userName = webSocketService.getAuthData(authToken).username();

            // Get game and see who is leaving
            GameData gameData = webSocketService.getGameData(gameId);

            // Depending on type of user, display message to user
            String message;
            if (Objects.equals(gameData.whiteUsername(), userName)) {
                message = String.format("Team white, %s, has left the game", userName);
            }
            else if (Objects.equals(gameData.blackUsername(), userName)) {
                message = String.format("Team black, %s, has left the game", userName);
            }
            else {
                message = String.format("%s has stopped observing", userName);
            }
            connections.remove(authToken);
            var serverMessage = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
            connections.broadcastToAll(serverMessage);
        }
        catch (Exception e) {
            var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.ERROR, null, null, e.getMessage());
            connections.broadcastToOne(authToken, serverMessageNotification);
        }
    }
}