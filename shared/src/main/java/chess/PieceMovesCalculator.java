package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface PieceMovesCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position);
}

/**
 * Returns a collection of all possible
 * pawn moves not including putting pawn
 */
class PawnMovesCalculator implements PieceMovesCalculator {
    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        int row = position.getRow();
        int column = position.getColumn();

        ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;

        int direction = (board.getPiece(position).pieceColor == white) ? 1 : -1;
        ChessGame.TeamColor enemy = (board.getPiece(position).pieceColor == white) ? black : white;

        ChessPosition forward = new ChessPosition(row + direction, column);
        ChessPosition twoForward = new ChessPosition(row + 2 * direction, column);
        ChessPosition rightDiag = new ChessPosition(row + direction, column + direction);
        ChessPosition leftDiag = new ChessPosition(row + direction, column - direction);

        if (forward.InBounds()) {
            if (board.getPiece(forward) == null) {
                getPawnPromotions(position, chessMoves, forward);
                if ((row == 2 || row == 7) && twoForward.InBounds() && board.getPiece(twoForward) == null) {
                    // Two Forward
                    chessMoves.add(new ChessMove(position, twoForward, null));
                }
            }
        }
        if (leftDiag.InBounds() && board.getPiece(leftDiag) != null && board.getPiece(leftDiag).pieceColor == enemy) {
            getPawnPromotions(position, chessMoves, leftDiag);
        }
        if (rightDiag.InBounds() && board.getPiece(rightDiag) != null && board.getPiece(rightDiag).pieceColor == enemy) {
            getPawnPromotions(position, chessMoves, rightDiag);
        }
        return chessMoves;

    }

    /**
     * Returns possible promotions of pawn and moves
     * <p>
     * @param position = starting position of the pawn
     * @param chessMoves = the collection of chess moves to add to
     * @param newPosition = the new position that we want to move the pawn
     */
    private void getPawnPromotions(ChessPosition position, Collection<ChessMove> chessMoves, ChessPosition newPosition) {
        if (newPosition.getRow() == 1 || newPosition.getRow() == 8) {
            // Move and Promote
            chessMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.QUEEN));
            chessMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.BISHOP));
            chessMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.ROOK));
            chessMoves.add(new ChessMove(position, newPosition, ChessPiece.PieceType.KNIGHT));
        }
        else {
            // Move
            chessMoves.add(new ChessMove(position, newPosition, null));
        }
    }
}

class KingMovesCalculator implements PieceMovesCalculator {
    private static final int[][] MOVES = {
            {1, 0}, {1, 1}, {0, 1}, {-1, -1},
            {0, -1}, {-1, 1}, {-1, 0}, {1, -1}
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        if (board.getPiece(position) != null) {
            ChessGame.TeamColor team = board.getPiece(position).pieceColor;
            return ChessMoveUtils.calculateFixedRangeMoves(board, position, MOVES, team);
        }
        return new ArrayList<>();
    }
}

class BishopMovesCalculator implements PieceMovesCalculator {
    private static final int[][] DIRECTIONS = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // Diagonal directions
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        if (board.getPiece(position) != null) {
            ChessGame.TeamColor team = board.getPiece(position).pieceColor;
            return ChessMoveUtils.calculateDirectionalMoves(board, position, DIRECTIONS, team);
        }
        return new ArrayList<>();
    }
}

class QueenMovesCalculator implements PieceMovesCalculator {
    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},  // Vertical and horizontal
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // Diagonal
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        if (board.getPiece(position) != null) {
            ChessGame.TeamColor team = board.getPiece(position).pieceColor;
            return ChessMoveUtils.calculateDirectionalMoves(board, position, DIRECTIONS, team);
        }
        return new ArrayList<>();
    }
}

class RookMovesCalculator implements PieceMovesCalculator {
    private static final int[][] DIRECTIONS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1} // Vertical and horizontal directions
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        if (board.getPiece(position) != null) {
            ChessGame.TeamColor team = board.getPiece(position).pieceColor;
            return ChessMoveUtils.calculateDirectionalMoves(board, position, DIRECTIONS, team);
        }
        return new ArrayList<>();
    }
}

class KnightMovesCalculator implements PieceMovesCalculator {
    private static final int[][] MOVES = {
            {2, 1}, {2, -1}, {1, 2}, {1, -2},
            {-2, 1}, {-2, -1}, {-1, 2}, {-1, -2}
    };

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        if (board.getPiece(position) != null) {
            ChessGame.TeamColor team = board.getPiece(position).pieceColor;
            return ChessMoveUtils.calculateFixedRangeMoves(board, position, MOVES, team);
        }
        return new ArrayList<>();
    }
}

