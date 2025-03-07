package dataaccess;

import chess.ChessGame;
import dataaccess.mysql.MySqlAuthDataAccess;
import dataaccess.mysql.MySqlGameDataAccess;
import dataaccess.mysql.MySqlUserDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DAOTests {
    DatabaseManager databaseManager;
    UserDAO userDAO ;
    GameDAO gameDAO ;
    AuthDAO authDAO ;
    UserData newUser;
    UserData newUser1;
    Integer newGameId;
    GameData newGame;
    ArrayList<GameData> gameList;
    String authToken;
    AuthData authData;

    public DAOTests() throws DataAccessException {
        this.databaseManager = new DatabaseManager();
        this.userDAO = new MySqlUserDataAccess(databaseManager);
        this.gameDAO = new MySqlGameDataAccess(databaseManager);
        this.authDAO = new MySqlAuthDataAccess(databaseManager);
        this.newUser = new UserData("newUser", "newPassword", "newuser@gmail.com");
        this.newUser1 = new UserData("newUser1", "newPassword1", "newuser@gmail.com");
    }

    @Test
    @Order(1)
    void validGameClear() {
        assertDoesNotThrow(() -> gameDAO.clear());
    }
    @Test
    @Order(2)
    void validAuthClear() {
        assertDoesNotThrow(() -> authDAO.clear());
    }

    @Test
    @Order(3)
    void validUserClear() {
        assertDoesNotThrow(() -> userDAO.clear());
    }

    @Test
    @Order(4)
    void validAddUser() {
        assertDoesNotThrow(() -> userDAO.addUser(newUser));
        assertDoesNotThrow(() -> userDAO.addUser(newUser1));
    }

    @Test
    @Order(5)
    void invalidAddUser() {
        assertThrows(DataAccessException.class, () -> userDAO.addUser(newUser));
    }

    @Test
    @Order(6)
    void validGetUser() {
        assertDoesNotThrow(() -> {userDAO.getUser("newUser"); });
    }

    @Test
    @Order(7)
    void invalidGetUser() {
        UserData user = assertDoesNotThrow(() -> userDAO.getUser(""), "getUser() should not throw an exception");
        assertNull(user, "Expected user to be null for an invalid input");
    }

    @Test
    @Order(8)
    void validCreateGame() {
        newGameId = assertDoesNotThrow(() -> gameDAO.createGame("newGame"));
    }

    // Since it will always create a game, what we can actually check is if it gets and authToken
    //because it checks that before creating a game
    @Test
    @Order(9)
    void invalidCreateGame() {
        authData = assertDoesNotThrow(() -> authDAO.getAuth("falseTokenForCreatingGame"));
        assertNull(authData);
    }

    @Test
    @Order(10)
    void validJoinGame() {
        assertDoesNotThrow(() -> gameDAO.updateGame(newGameId, ChessGame.TeamColor.WHITE, "newUser"));
    }

    @Test
    @Order(11)
    void invalidJoinGame() {
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(newGameId, ChessGame.TeamColor.WHITE, "' OR 1=1; --"));
    }

    @Test
    @Order(12)
    void validGetGame() {
        assertDoesNotThrow(() -> {gameDAO.getGame(newGameId); });
    }

    @Test
    @Order(13)
    void validListGame() {
        gameList = assertDoesNotThrow(() -> gameDAO.listGames());
        assertFalse(gameList.isEmpty());
    }

    @Test
    @Order(14)
    void invalidListGame() {
        assertDoesNotThrow(() -> gameDAO.clear());
        gameList = assertDoesNotThrow(() -> gameDAO.listGames());
        assertTrue(gameList.isEmpty());
    }

    @Test
    @Order(15)
    void invalidGetGame() {
        newGame = assertDoesNotThrow(() -> gameDAO.getGame(newGameId));
    }

    @Test
    @Order(16)
    void invalidGetAuth() {
        authData = assertDoesNotThrow(() -> authDAO.getAuth("falseToken") );
        assertNull(authData);
    }

    @Test
    @Order(17)
    void validCreateAuth() {
        assertDoesNotThrow(() -> authDAO.createAuth(new AuthData("1", "newUser")));
    }

    @Test
    @Order(18)
    void validGetAuth() {
        authData = assertDoesNotThrow(() -> authDAO.getAuth("1"));
        assertEquals("1", authData.authToken());
    }

    @Test
    @Order(19)
    void invalidCreateAuth() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(new AuthData("1", "' OR 1=1; --")));
    }

    @Test
    @Order(20)
    void validDeleteAuth() {
        assertDoesNotThrow(() -> authDAO.deleteAuth(authToken));
    }

    // We will test finding the authToken because delete auth will not throw or return anything
    @Test
    @Order(21)
    void invalidDeleteAuth() {
        authData = assertDoesNotThrow(() -> authDAO.getAuth("falseAuthTokenForDeleting"));
        assertNull(authData);
    }
}
