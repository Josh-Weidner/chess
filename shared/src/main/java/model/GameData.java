package model;

import chess.ChessGame;

public record GameData(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData withWhiteUsername(String username) {
        if (this.whiteUsername != null && username != null) {
            throw new IllegalStateException("White username can only be set once.");
        }
        return new GameData(gameID, username, blackUsername, gameName, game);
    }

    public GameData withBlackUsername(String username) {
        if (this.blackUsername != null && username != null) {
            throw new IllegalStateException("Black username can only be set once.");
        }
        return new GameData(gameID, whiteUsername, username, gameName, game);
    }
}
