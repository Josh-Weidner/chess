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
    private final GameDAO gameDAO;

    private final AuthService authService;

    public GameService(GameDAO gameDAO, AuthService authService) {
        this.gameDAO = gameDAO;

        this.authService = authService;
    }

    public ListResult gameList(String authToken) throws Exception {
        try {
            // if auth is not found in database error will be thrown
            authService.getAuthData(authToken);
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        // initialize our new list and the list from the database
        ArrayList<GameDataModel> gameDataModelList = new ArrayList<>();
        ArrayList<GameData> games = gameDAO.listGames();

        // convert database data to model for user
        for (GameData gameData : games) {
            gameDataModelList.add(new GameDataModel(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName()));
        }

        return new ListResult(gameDataModelList);
    }

    public void joinGame(String authToken, JoinRequest joinRequest) throws Exception {
        try {
            // if auth is not found in database error will be thrown
            AuthData authData = authService.getAuthData(authToken);

            // if game is not found in database error will be thrown
            GameData gameData = gameDAO.getGame(joinRequest.gameID());

            // check if the corresponding team is occupied
            if ((joinRequest.playerColor() == ChessGame.TeamColor.BLACK && gameData.blackUsername() != null) || (joinRequest.playerColor() == ChessGame.TeamColor.WHITE && gameData.whiteUsername() != null)) {
                throw new Exception("already taken");
            }

            gameDAO.updateGame(gameData.gameID(), joinRequest.playerColor(), authData.username());
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public CreateResult createGame(String authToken, CreateRequest createRequest) throws Exception {
        try {
            // if auth is not found in database error will be thrown
            authService.getAuthData(authToken);
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }

        Integer gameId = gameDAO.createGame(createRequest.gameName());

        return new CreateResult(gameId);
    }
}
