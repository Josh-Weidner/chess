package client;

import chess.ChessGame;
import exception.ResponseException;
import model.create.CreateRequest;
import model.join.JoinRequest;
import model.login.LoginRequest;
import model.register.RegisterRequest;
import model.register.RegisterResult;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static final RegisterRequest registerRequest = new RegisterRequest("newUser", "newPassword", "newEmail");
    private static final CreateRequest createRequest = new CreateRequest("newGame");
    private static String newAuthToken = "";
    private static int newGameId;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }


    @Test
    @Order(1)
    void validClear() {assertDoesNotThrow(() -> facade.clearDatabase());
    }

    @Test
    @Order(2)
    void validRegister() {
        RegisterResult registerResult = assertDoesNotThrow(() -> facade.registerUser(registerRequest));
        newAuthToken = registerResult.authToken();
    }

    @Test
    @Order(3)
    void invalidRegister() {assertThrows(ResponseException.class, () -> facade.registerUser(registerRequest));
    }

    @Test
    @Order(4)
    void invalidLogin() {
        assertThrows(ResponseException.class, () -> facade.loginUser(new LoginRequest("badNewUser", "badPassword")));
    }

    @Test
    @Order(5)
    void invalidLogout(){
        assertThrows(ResponseException.class, () -> facade.logoutUser(" "));
    }

    @Test
    @Order(6)
    void validCreateGame() {
        var createResult = assertDoesNotThrow(() -> facade.createGame(createRequest, newAuthToken));
        newGameId = createResult.gameID();
    }

    @Test
    @Order(7)
    void invalidCreateGame() {
        assertThrows(ResponseException.class, () -> facade.createGame(createRequest, ""));
    }

    // Since it will always create a game, what we can actually check is if it gets and authToken
    //because it checks that before creating a game
    @Test
    @Order(8)
    void validListGames() {
        assertDoesNotThrow(() -> facade.listGames(newAuthToken));
    }

    @Test
    @Order(9)
    void invalidListGames() {
        assertThrows(ResponseException.class, () -> facade.listGames(""));
    }

    @Test
    @Order(10)
    void validJoinGame() {
        JoinRequest joinRequest = new JoinRequest(ChessGame.TeamColor.WHITE, newGameId);
        assertDoesNotThrow(() -> facade.joinGame(joinRequest, newAuthToken));
    }

    @Test
    @Order(11)
    void invalidJoinGame() {
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinRequest(ChessGame.TeamColor.WHITE, newGameId), newAuthToken));
    }

    @Test
    @Order(12)
    void validLogout() {
        assertDoesNotThrow(() -> facade.logoutUser(newAuthToken));
    }

    @Test
    @Order(13)
    void validLogin() {
        assertDoesNotThrow(() -> facade.loginUser(new LoginRequest("newUser", "newPassword")));
    }

    @AfterAll
    static void stopServer() throws ResponseException {
        facade.clearDatabase();
        server.stop();
    }

}
