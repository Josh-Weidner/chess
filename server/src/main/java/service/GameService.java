package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import server.ResponseException;
import service.create.CreateRequest;
import service.create.CreateResult;
import service.join.JoinRequest;
import service.list.GameDataModel;
import service.list.ListResult;

import java.util.ArrayList;

public class GameService {
    private final GameDAO gameDAO;

    private final AuthService authService;

    public GameService(GameDAO gameDAO, AuthService authService) {
        this.gameDAO = gameDAO;

        this.authService = authService;
    }

    public ListResult gameList(String authToken) throws ResponseException, DataAccessException {
        authService.getAuthData(authToken);

        // initialize our new list and the list from the database
        ArrayList<GameDataModel> gameDataModelList = new ArrayList<>();
        ArrayList<GameData> games = gameDAO.listGames();

        // convert database data to model for user
        for (GameData gameData : games) {
            gameDataModelList.add(new GameDataModel(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName()));
        }

        return new ListResult(gameDataModelList);
    }

    public void joinGame(String authToken, JoinRequest joinRequest) throws ResponseException, DataAccessException {
        if (authToken == null || joinRequest == null || joinRequest.gameID() == null || joinRequest.playerColor() == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        AuthData authData = authService.getAuthData(authToken);

        // if game is not found in database error will be thrown
        GameData gameData = gameDAO.getGame(joinRequest.gameID());

        // check if the corresponding team is occupied
        if ((joinRequest.playerColor() == ChessGame.TeamColor.BLACK && gameData.blackUsername() != null) ||
                (joinRequest.playerColor() == ChessGame.TeamColor.WHITE && gameData.whiteUsername() != null)) {
            throw new ResponseException(403, "Error: already taken");
        }

        gameDAO.updateGame(gameData.gameID(), joinRequest.playerColor(), authData.username());
    }

    public CreateResult createGame(String authToken, CreateRequest createRequest) throws ResponseException, DataAccessException {
        if (authToken == null || createRequest == null || createRequest.gameName() == null) {
            throw new ResponseException(400, "Error: bad request");
        }

        authService.getAuthData(authToken);

        Integer gameId = gameDAO.createGame(createRequest.gameName());

        return new CreateResult(gameId);
    }
}
