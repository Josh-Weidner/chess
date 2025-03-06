package dataaccess.mysql;

import dataaccess.DatabaseManager;
import dataaccess.UserDAO;
import model.UserData;
import server.ResponseException;
import org.mindrot.jbrypt.BCrypt;

import java.sql.*;

public class MySqlUserDataAccess implements UserDAO {
    private final DatabaseManager databaseManager;

    public MySqlUserDataAccess(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        databaseManager.configureDatabase();
    }

    public void addUser(UserData user) throws ResponseException {
        var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        var hashedPass = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        databaseManager.executeUpdate(statement, user.username(), hashedPass, user.email());
    }

    public UserData getUser(String username) throws ResponseException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM users WHERE username=?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResponseException(500, String.format("Unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    public void clear() throws ResponseException {
        var statement = "DELETE FROM users";
        databaseManager.executeUpdate(statement);
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }
}
