package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Connection>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameId, String authToken, Session session) {
        var connection = new Connection(authToken, session);
        connections.computeIfAbsent(gameId, k -> new ConcurrentHashMap<>())
                .put(authToken, connection);
    }

    public void remove(Integer gameId, String authToken) {
        connections.get(gameId).remove(authToken);
    }

    public void broadcastAndExcludeOne(Integer gameId, String excludeAuthToken, ServerMessage message) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameId).values()) {
            if (c.session.isOpen()) {
                if (!c.authToken.equals(excludeAuthToken)) {
                    c.send(new Gson().toJson(message));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.get(gameId).remove(c.authToken);
        }
    }

    public void broadcastToOne(Integer gameId, String authToken, ServerMessage message) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameId).values()) {
            if (c.session.isOpen()) {
                if (c.authToken.equals(authToken)) {
                    System.out.println(message.toString());
                    c.send(new Gson().toJson(message));
                }
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.get(gameId).remove(c.authToken);
        }
    }

    public void broadcastToAll(Integer gameId, ServerMessage message) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (var c : connections.get(gameId).values()) {
            if (c.session.isOpen()) {
                c.send(new Gson().toJson(message));
            } else {
                removeList.add(c);
            }
        }

        // Clean up any connections that were left open.
        for (var c : removeList) {
            connections.get(gameId).remove(c.authToken);
        }
    }
}
