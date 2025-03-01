package service;

import chess.ChessGame;
import dataaccess.*;
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

    UserDAO userDAO = new UserMemoryDataAccess();
    GameDAO gameDAO = new GameMemoryDataAccess();
    AuthDAO authDAO = new AuthMemoryDataAccess();
    UserService userService = new UserService(userDAO, authDAO, gameDAO);
    AuthService authService = new AuthService(authDAO);
    GameService gameService = new GameService(gameDAO, authService);

    private String newAuth = "";
    private Integer newGameId = 0;

    @Test
    @Order(1)
    void validRegister() throws ResponseException {
        RegisterResult result = userService.register(new RegisterRequest("newUser", "newPassword", "newEmail@gmail.com"));
        newAuth = result.authToken();
        assertEquals("newUser", result.username());
        assertNotEquals(null, result.authToken());
    }

    @Test
    @Order(2)
    void invalidRegister() {
        ResponseException exception = assertThrows(ResponseException.class,
                () -> userService.register(new RegisterRequest("newUser", "newPassword", "newEmail@gmail.com")));

        assertEquals(403, exception.statusCode()); // Check the status code if ResponseException has one
        assertEquals("Error: already taken", exception.getMessage()); // Check the error message
    }

    @Test
    @Order(3)
    void invalidLogout() {
        ResponseException exception = assertThrows(ResponseException.class, () -> authService.deleteAuthData(""));
        assertEquals(401, exception.statusCode());
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @Order(4)
    void validLogout() {
        assertDoesNotThrow(() -> authService.deleteAuthData(newAuth));
    }

    @Test
    @Order(5)
    void invalidLogin() {
        ResponseException exception = assertThrows(ResponseException.class, () -> userService.login(new LoginRequest("newUser", "wrongPassword")));
        assertEquals(401, exception.statusCode());
        assertEquals("Error: unauthorized", exception.getMessage());
    }

    @Test
    @Order(6)
    void validLogin() throws ResponseException {
        LoginResult result = userService.login(new LoginRequest("newUser", "newPassword"));
        newAuth = result.authToken();
        assertEquals("newUser", result.username());
        assertNotEquals(null, result.authToken());
    }

    @Test
    @Order(7)
    void validCreate() throws ResponseException {
        CreateResult result = gameService.createGame(newAuth,new CreateRequest("newGame"));
        newGameId = result.gameID();
        assertNotNull(result.gameID());
    }

    @Test
    @Order(8)
    void invalidCreate() {
        ResponseException responseException = assertThrows(ResponseException.class,
                () -> gameService.createGame("",new CreateRequest("anotherGame")));
        assertEquals(401, responseException.statusCode());
        assertEquals("Error: unauthorized", responseException.getMessage());
    }

    @Test
    @Order(9)
    void validJoin() {
        assertDoesNotThrow(() -> gameService.joinGame(newAuth, new JoinRequest(ChessGame.TeamColor.WHITE, newGameId)));
    }

    @Test
    @Order(10)
    void invalidJoin() {
        ResponseException responseException = assertThrows(ResponseException.class,
                () -> gameService.joinGame(newAuth,new JoinRequest(ChessGame.TeamColor.WHITE, newGameId)));
        assertEquals(403, responseException.statusCode());
        assertEquals("Error: already taken", responseException.getMessage());
    }

    @Test
    @Order(11)
    void validListGames() {
        ListResult result = assertDoesNotThrow(() -> gameService.gameList(newAuth));

        assertNotNull(result); // Ensure result is not null
        assertEquals(1, result.games().size(), "Expected exactly 1 game in the list");
    }

    @Test
    @Order(12)
    void invalidListGames() {
        ResponseException responseException = assertThrows(ResponseException.class, () -> gameService.gameList(""));
        assertEquals(401, responseException.statusCode());
        assertEquals("Error: unauthorized", responseException.getMessage());
    }

    @Test
    @Order(13)
    void validClear() {
        userService.clear();
        ResponseException responseException = assertThrows(ResponseException.class,
                () -> userService.login(new LoginRequest("newUser", "newPassword")));
        assertEquals(401, responseException.statusCode());
        assertEquals("Error: unauthorized", responseException.getMessage());
    }
}
