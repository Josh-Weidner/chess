package dataaccess;

import java.sql.*;
import java.util.Properties;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class DatabaseManager {
    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        try {
            try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
                if (propStream == null) {
                    throw new Exception("Unable to load db.properties");
                }
                Properties props = new Properties();
                props.load(propStream);
                DATABASE_NAME = props.getProperty("db.name");
                USER = props.getProperty("db.user");
                PASSWORD = props.getProperty("db.password");

                var host = props.getProperty("db.host");
                var port = Integer.parseInt(props.getProperty("db.port"));
                CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);
            }
        } catch (Exception ex) {
            throw new RuntimeException("unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    public static void createDatabase() throws DataAccessException {
        try {
            var statement = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DbInfo.getConnection(databaseName)) {
     * // execute SQL statements.
     * }
     * </code>
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            conn.setCatalog(DATABASE_NAME);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = getConnection();
             var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {

            setParameters(ps, params);

            ps.executeUpdate();
            return getGeneratedKey(ps);

        } catch (Exception e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private void setParameters(PreparedStatement ps, Object... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object param = params[i];

            switch (param) {
                case null -> ps.setNull(i + 1, Types.NULL);
                case String s -> ps.setString(i + 1, s);
                case Integer integer -> ps.setInt(i + 1, integer);
                default -> {
                }
            }
            // Add more type checks as needed
        }
    }

    private int getGeneratedKey(PreparedStatement ps) throws SQLException {
        try (var rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    public void configureDatabase() throws DataAccessException {
        try { createDatabase();
            try (var conn = DatabaseManager.getConnection()) {
                for (var statement : createStatements) {
                    try (var preparedStatement = conn.prepareStatement(statement)) {
                        preparedStatement.executeUpdate();
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
    CREATE TABLE IF NOT EXISTS users (
      `username` varchar(255) PRIMARY KEY,
      `password` varchar(255) NOT NULL,
      `email` varchar(255) NOT NULL
    )
    """,
            """
    CREATE TABLE IF NOT EXISTS games (
      `gameId` int AUTO_INCREMENT PRIMARY KEY,
      `gameName` varchar(255) NOT NULL,
      `whiteUsername` varchar(255),
      `blackUsername` varchar(255),
      `game` TEXT NOT NULL,
      FOREIGN KEY (whiteUsername) REFERENCES users(username),
      FOREIGN KEY (blackUsername) REFERENCES users(username)
    )
    """,
            """
    CREATE TABLE IF NOT EXISTS auth (
      `authToken` varchar(255) PRIMARY KEY,
      `username` varchar(255),
      FOREIGN KEY (username) REFERENCES users(username)
    )
    """
    };
}
