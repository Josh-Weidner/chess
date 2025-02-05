package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard chessBoard;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if (piece == null) { return null; }

        Collection<ChessMove> moves = piece.pieceMoves(chessBoard, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();

        for (ChessMove move : moves) {
            // Create a copy of the board
            ChessGame tempGame = new ChessGame();
            tempGame.setTeamTurn(getTeamTurn());
            for (int i = 0; i < chessBoard.chessBoard.length; i++) {
                for (int j = 0; j < chessBoard.chessBoard[i].length; j++) {
                    ChessPosition position = new ChessPosition(i + 1, j + 1);
                    ChessPiece newPiece = chessBoard.getPiece(position);
                    tempGame.chessBoard.addPiece(position, newPiece);
                }
            }

            // Apply the move on the temp board
            try {
                tempGame.makeTempMove(move);
            } catch (InvalidMoveException e){
                continue;
            }

            // Check if the king is still safe
            if (!tempGame.isInCheck(piece.getTeamColor())) {
                validMoves.add(move); // Add only if king is not in check
            }
        }

        return validMoves;
    }

    /**
     * Gets all valid moves for a team
     *
     * @param teamColor the team to get valid moves for
     * @return Set of valid moves for requested team
     */
    public Collection<ChessMove> allValidMoves(ChessGame.TeamColor teamColor) {
        ArrayList<ChessMove> legalMoves = new ArrayList<>();
        for (int i = 0; i < chessBoard.chessBoard.length; i++) {
            for (int j = 0; j < chessBoard.chessBoard[i].length; j++) {
                ChessPosition position = new ChessPosition(i + 1, j + 1);
                ChessPiece piece = chessBoard.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceMoves = validMoves(position);
                    legalMoves.addAll(pieceMoves);
                }
            }
        }
        return legalMoves;
    }

    /**
     * Gets all valid moves for a team
     *
     * @param teamColor the team to get valid moves for
     * @return Set of valid moves for requested team
     */
    public Collection<ChessMove> allPossibleMoves(ChessGame.TeamColor teamColor) {
        ArrayList<ChessMove> possibleMoves = new ArrayList<>();
        for (int i = 0; i < chessBoard.chessBoard.length; i++) {
            for (int j = 0; j < chessBoard.chessBoard[i].length; j++) {
                ChessPosition position = new ChessPosition(i + 1, j + 1);
                ChessPiece piece = chessBoard.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> pieceMoves = piece.pieceMoves(chessBoard, position);
                    possibleMoves.addAll(pieceMoves);
                }
            }
        }
        return possibleMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException{
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        if (getTeamTurn() != piece.getTeamColor()) {
            throw new InvalidMoveException();
        }
        chessBoard.addPiece(move.getStartPosition(), null);
        ChessPiece.PieceType promotionType = move.getPromotionPiece();
        if (promotionType != null) {
            piece.type = promotionType;
        }
        chessBoard.addPiece(move.getEndPosition(), piece);
    }

    /**
     * Makes a move in a temp chess game
     *
     * @param move temp chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeTempMove(ChessMove move) throws InvalidMoveException{
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        chessBoard.addPiece(move.getStartPosition(), null);
        chessBoard.addPiece(move.getEndPosition(), piece);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        TeamColor enemy = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        ChessPosition kingPosition = chessBoard.getKing(teamColor);
        Collection<ChessMove> enemyMoves = allPossibleMoves(enemy);
        for (ChessMove enemyMove: enemyMoves) {
            if (enemyMove.getEndPosition().equals(kingPosition)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return allValidMoves(teamColor).isEmpty();
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return allValidMoves(teamColor).isEmpty();
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }
}
