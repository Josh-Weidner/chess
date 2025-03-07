package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.mysql.MySqlAuthDataAccess;
import dataaccess.mysql.MySqlGameDataAccess;
import dataaccess.mysql.MySqlUserDataAccess;
import org.junit.jupiter.api.*;
import server.ResponseException;
import service.create.CreateRequest;
import service.create.CreateResult;
import service.join.JoinRequest;
import service.list.ListResult;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTests {

    DatabaseManager databaseManager = new DatabaseManager();
    UserDAO userDAO = new MySqlUserDataAccess(databaseManager);
    GameDAO gameDAO = new MySqlGameDataAccess(databaseManager);
    AuthDAO authDAO = new MySqlAuthDataAccess(databaseManager);
    UserService userService = new UserService(userDAO, authDAO, gameDAO);
    AuthService authService = new AuthService(authDAO);
    GameService gameService = new GameService(gameDAO, authService);

    private String newAuth = "";
    private Integer newGameId = 0;

    public UserServiceTests() throws DataAccessException {
    }

    @Test
    @Order(1)
    void validStartClear() {
        assertDoesNotThrow(() -> userService.clear());
    }

    @Test
    @Order(2)
    void validRegister() throws ResponseException, DataAccessException {
        RegisterResult result = userService.register(new RegisterRequest("newUser", "newPassword", "newEmail@gmail.com"));
        newAuth = result.authToken();
        assertEquals("newUser", result.username());
        assertNotEquals(null, result.authToken());
    }

    @Test
    @Order(3)
    void invalidRegister() {
        ResponseException exception = assertThrows(ResponseException.class,
                () -> userService.register(new RegisterRequest("newUser", "newPassword", "newEmail@gmail.com")));

        assertEquals(403, exception.statusCode()); // Check the status code if ResponseException has one
        assertEquals("Error: already taken", exception.getMessage()); // Check the error message
    }

    @Test
    @Order(4)
    void invalidLogout() {
        ResponseException exception = assertThrows(ResponseException.class, () -> authService.deleteAuthData(""));
        assertEquals(401, exception.statusCode());
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @Order(5)
    void validLogout() {
        assertDoesNotThrow(() -> authService.deleteAuthData(newAuth));
    }

    @Test
    @Order(6)
    void invalidLogin() {
        ResponseException exception = assertThrows(ResponseException.class, () -> userService.login(new LoginRequest("newUser", "wrongPassword")));
        assertEquals(401, exception.statusCode());
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @Order(7)
    void validLogin() throws ResponseException, DataAccessException {
        LoginResult result = userService.login(new LoginRequest("newUser", "newPassword"));
        newAuth = result.authToken();
        assertEquals("newUser", result.username());
        assertNotEquals(null, result.authToken());
    }

    @Test
    @Order(8)
    void validCreate() throws ResponseException, DataAccessException {
        CreateResult result = gameService.createGame(newAuth,new CreateRequest("newGame"));
        newGameId = result.gameID();
        assertNotNull(result.gameID());
    }

    @Test
    @Order(9)
    void invalidCreate() {
        ResponseException responseException = assertThrows(ResponseException.class,
                () -> gameService.createGame("",new CreateRequest("anotherGame")));
        assertEquals(401, responseException.statusCode());
        assertEquals("Error: unauthorized", responseException.getMessage());
    }

    @Test
    @Order(10)
    void validJoin() {
        assertDoesNotThrow(() -> gameService.joinGame(newAuth, new JoinRequest(ChessGame.TeamColor.WHITE, newGameId)));
    }

    @Test
    @Order(11)
    void invalidJoin() {
        ResponseException responseException = assertThrows(ResponseException.class,
                () -> gameService.joinGame(newAuth,new JoinRequest(ChessGame.TeamColor.WHITE, newGameId)));
        assertEquals(403, responseException.statusCode());
        assertEquals("Error: already taken", responseException.getMessage());
    }

    @Test
    @Order(12)
    void validListGames() {
        ListResult result = assertDoesNotThrow(() -> gameService.gameList(newAuth));

        assertNotNull(result); // Ensure result is not null
        assertEquals(1, result.games().size(), "Expected exactly 1 game in the list");
    }

    @Test
    @Order(13)
    void invalidListGames() {
        ResponseException responseException = assertThrows(ResponseException.class, () -> gameService.gameList(""));
        assertEquals(401, responseException.statusCode());
        assertEquals("Error: unauthorized", responseException.getMessage());
    }

    @Test
    @Order(14)
    void validClear() throws DataAccessException {
        userService.clear();
        ResponseException responseException = assertThrows(ResponseException.class,
                () -> userService.login(new LoginRequest("newUser", "newPassword")));
        assertEquals(401, responseException.statusCode());
        assertEquals("Error: unauthorized", responseException.getMessage());
    }
}
