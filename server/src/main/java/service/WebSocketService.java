package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import model.login.LoginRequest;
import model.login.LoginResult;
import model.register.RegisterRequest;
import model.register.RegisterResult;
import org.mindrot.jbcrypt.BCrypt;

public class WebSocketService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public WebSocketService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public AuthData getAuthData(String token) throws ResponseException, DataAccessException {
        AuthData authData = authDAO.getAuth(token);
        if (authData == null) {
            throw new ResponseException(401, "Error: unauthorized");
        }
        return authData;
    }

    public GameData getGameData(int Id) throws ResponseException, DataAccessException {
        GameData gameData = gameDAO.getGame(Id);
        if (gameData == null) {
            throw new ResponseException(401, "Error: Game not found");
        }
        return gameData;
    }
}
