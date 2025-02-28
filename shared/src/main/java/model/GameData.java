package model;

import chess.ChessGame;

public record GameData() {
    static Integer gameID;
    static String whiteUsername;
    static String blackUsername;
    static String gameName;
    static ChessGame game;
}
