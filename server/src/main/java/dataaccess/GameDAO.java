package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;

public interface GameDAO {
    Integer createGame(String gameName);
    void updateGame(Integer gameId, ChessGame.TeamColor teamColor, String userName);
    GameData getGame(Integer gameId);
    ArrayList<GameData> listGames();
    void clear();

}
