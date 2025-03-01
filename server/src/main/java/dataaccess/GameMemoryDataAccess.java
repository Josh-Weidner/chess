package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;

public class GameMemoryDataAccess implements GameDAO{
    private int nextId = 1;
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public void clear() {
        games.clear();
    }

    public ArrayList<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    public Integer createGame(String gameName) {
        Integer id = nextId++;
        GameData game = new GameData(id, null, null, gameName, new ChessGame());
        games.put(id, game);
        return id;
    }

    public GameData getGame(Integer gameId){
        return games.get(gameId);
    }

    public void updateGame(Integer gameId, ChessGame.TeamColor teamColor, String userName){

        GameData gameData = games.get(gameId); // Get the existing object

        if (teamColor == ChessGame.TeamColor.BLACK) {
            games.put(gameId, gameData.withBlackUsername(userName)); // Store the new object
        }

        if (teamColor == ChessGame.TeamColor.WHITE) {
            games.put(gameId, gameData.withWhiteUsername(userName)); // Store the new object
        }
    }
}
