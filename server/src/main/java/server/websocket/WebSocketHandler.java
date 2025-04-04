package server.websocket;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connect(command.getAuthToken(), session);
            case MAKE_MOVE -> makeMove(command.getAuthToken(), command.getGameID(), command.getMove());
            case RESIGN -> resign(command.getAuthToken());
            case LEAVE -> leave(command.getAuthToken(), command.getGameID());
        }
    }

    private void connect(String authToken, Session session) throws IOException {
        // Get user

        connections.add(authToken, session);

        // Get game and check if user is observer, white, or black and display the message depending on that

        var message = String.format("%s has joined", authToken);
        var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
        connections.broadcast(authToken, serverMessageNotification);
    }

    private void makeMove(String authToken, int gameId, ChessMove move) throws IOException {

        // Get player

        // Get Game

        // Verify player in game and move throw error potentially

        // change turn

        var message = String.format("%s made a move", authToken);
        var serverMessageNotification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION, null, null, message);
        connections.broadcast(authToken, serverMessageNotification);
        var serverMessageLoadGame = new ServerMessage(ServerMessage.ServerMessageType.LOAD_GAME, game, null, null);
        connections.broadcast(authToken, serverMessageLoadGame);
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