package dataaccess;

import chess.ChessGame;
import model.GameData;
import server.ResponseException;

import java.util.ArrayList;

public interface GameDAO {
    Integer createGame(String gameName) throws ResponseException;
    void updateGame(Integer gameId, ChessGame.TeamColor teamColor, String userName) throws ResponseException;
    GameData getGame(Integer gameId) throws ResponseException;
    ArrayList<GameData> listGames() throws ResponseException;
    void clear() throws ResponseException;

}
