package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    Integer createGame(String gameName) throws DataAccessException;
    void updateGame(Integer gameId, ChessGame.TeamColor teamColor, String userName) throws DataAccessException;
    GameData getGame(Integer gameId) throws DataAccessException;
    ArrayList<GameData> listGames() throws DataAccessException;
    void clear() throws DataAccessException;

}
