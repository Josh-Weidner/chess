package dataaccess.mysql;

import dataaccess.AuthDAO;
import dataaccess.DatabaseManager;
import model.AuthData;

import java.sql.*;

public class MySqlAuthDataAccess implements AuthDAO {
    private final DatabaseManager databaseManager;

    public MySqlAuthDataAccess(DatabaseManager databaseManager) throws server.ResponseException {
        this.databaseManager = databaseManager;
        databaseManager.configureDatabase();
    }

    public void createAuth(AuthData authData) throws server.ResponseException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        databaseManager.executeUpdate(statement, authData.authToken(), authData.username());
    }

    public AuthData getAuth(String authToken) throws server.ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auth WHERE authToken=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new server.ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void deleteAuth(String authToken) throws server.ResponseException {
        var statement = "DELETE FROM auth WHERE authToken=?";
        databaseManager.executeUpdate(statement, authToken);
    }

    public void clear() throws server.ResponseException {
        var statement = "DELETE FROM auth";
        databaseManager.executeUpdate(statement);
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        return new AuthData(authToken, username);
    }
}
