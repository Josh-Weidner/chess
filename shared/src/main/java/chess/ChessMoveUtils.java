package chess;


import java.util.Collection;
import java.util.ArrayList;

public class ChessMoveUtils {

    public static Collection<ChessMove> calculateDirectionalMoves(
            ChessBoard board,
            ChessPosition position,
            int[][] directions,
            ChessGame.TeamColor team
    ) {
        Collection<ChessMove> chessMoves = new ArrayList<>();
        int row = position.getRow();
        int column = position.getColumn();

        for (int[] direction : directions) {
            int currentRow = row;
            int currentCol = column;

            while (true) {
                currentRow += direction[0];
                currentCol += direction[1];
                ChessPosition nextPosition = new ChessPosition(currentRow, currentCol);

                if (!nextPosition.inBounds()) {
                    break; // Out of bounds
                }

                ChessPiece targetPiece = board.getPiece(nextPosition);

                if (targetPiece == null) {
                    // Empty square
                    chessMoves.add(new ChessMove(position, nextPosition, null));
                } else {
                    if (targetPiece.pieceColor != team) {
                        // Capture enemy piece
                        chessMoves.add(new ChessMove(position, nextPosition, null));
                    }
                    break; // Stop after hitting any piece
                }
            }
        }

        return chessMoves;
    }
    public static Collection<ChessMove> calculateFixedRangeMoves(
            ChessBoard board,
            ChessPosition position,
            int[][] moves,
            ChessGame.TeamColor team
    ) {
        Collection<ChessMove> chessMoves = new ArrayList<>();

        for (int[] move : moves) {
            int newRow = position.getRow() + move[0];
            int newCol = position.getColumn() + move[1];
            ChessPosition nextPosition = new ChessPosition(newRow, newCol);

            if (nextPosition.inBounds()) {
                ChessPiece targetPiece = board.getPiece(nextPosition);

                if (targetPiece == null) {
                    chessMoves.add(new ChessMove(position, nextPosition, null));
                }
                else if (targetPiece.pieceColor != team) {
                    chessMoves.add(new ChessMove(position, nextPosition, null));
                }
            }
        }

        return chessMoves;
    }
}
