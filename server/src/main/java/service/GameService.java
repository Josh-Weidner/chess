package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.Create.CreateRequest;
import service.Create.CreateResult;
import service.Join.JoinRequest;
import service.List.GameDataModel;
import service.List.ListResult;

import java.util.ArrayList;

public class GameService {
    private final AuthService authService;
    private final GameDAO gameDAO;

    public GameService(AuthService authService, GameDAO gameDAO) {
        this.authService = authService;
        this.gameDAO = gameDAO;
    }

    public ListResult gameList(String authToken) throws Exception {
        ArrayList<GameDataModel> gameDataModelList = new ArrayList<>();

        AuthData authData = authService.getAuthData(authToken);
        if (authData == null) {
            throw new Exception("unauthorized");
        }

        ArrayList<GameData> games = gameDAO.listGames();

        for (GameData gameData : games) {
            gameDataModelList.add(new GameDataModel(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName()));
        }

        return new ListResult(gameDataModelList);
    }
    public void joinGame(String authToken, JoinRequest joinRequest) throws Exception {
        AuthData authData = authService.getAuthData(authToken);
        if (authData == null) {
            throw new Exception("unauthorized");
        }

        GameData gameData = gameDAO.getGame(joinRequest.gameID());
        if (gameData == null || (joinRequest.playerColor() == ChessGame.TeamColor.BLACK && gameData.blackUsername() != null) || (joinRequest.playerColor() == ChessGame.TeamColor.WHITE && gameData.whiteUsername() != null)) {
            throw new Exception("already taken");
        }

        gameDAO.updateGame(gameData.gameID(), joinRequest.playerColor(), authData.username());
    }
    public CreateResult createGame(String authToken, CreateRequest createRequest) throws Exception {
        AuthData authData = authService.getAuthData(authToken);
        if (authData == null) {
            throw new Exception("unauthorized");
        }

        Integer gameId = gameDAO.createGame(createRequest.gameName());

        return new CreateResult(gameId);
    }
}
