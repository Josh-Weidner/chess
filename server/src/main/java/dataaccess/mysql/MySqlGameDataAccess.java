package dataaccess.mysql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.sql.*;

public class MySqlGameDataAccess implements GameDAO {
    private final DatabaseManager databaseManager;

    public MySqlGameDataAccess(DatabaseManager databaseManager) throws DataAccessException {
        this.databaseManager = databaseManager;
        databaseManager.configureDatabase();
    }

    public Integer createGame(String gameName) throws DataAccessException {
        var statement = "INSERT INTO games (gameName, game) VALUES (?, ?)";
        var json = new Gson().toJson(new ChessGame());
        return databaseManager.executeUpdate(statement, gameName, json);
    }

    public GameData getGame(Integer gameId) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games WHERE gameId=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameId);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void updateGame(Integer gameId, ChessGame.TeamColor teamColor, String userName) throws DataAccessException {
        var team = (teamColor == ChessGame.TeamColor.BLACK) ? "blackUsername" : "whiteUsername";
        var statement = "UPDATE games Set " + team + " = ? WHERE gameId = ?";
        databaseManager.executeUpdate(statement, userName, gameId);
    }

    public ArrayList<GameData> listGames() throws DataAccessException {
        ArrayList<GameData> games = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM games";
            try (var ps = conn.prepareStatement(statement);
                 var rs = ps.executeQuery()) {
                while (rs.next()) {
                    games.add(readGame(rs));
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Unable to read data: %s", e.getMessage()));
        }
        return games;
    }

    public void saveGame(GameData gameData) throws DataAccessException {
        var statement = "UPDATE games SET game = ? WHERE gameId = ?";
        var json = new Gson().toJson(gameData.game());
        databaseManager.executeUpdate(statement, json, gameData.gameID());
    }

    public void clear() throws DataAccessException {
        var statement = "DELETE FROM games";
        databaseManager.executeUpdate(statement);
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        var gameId = rs.getInt("gameId");
        var gameName = rs.getString("gameName");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var game = new Gson().fromJson(rs.getString("game"), ChessGame.class);
        return new GameData(gameId, whiteUsername, blackUsername, gameName, game);
    }
}
