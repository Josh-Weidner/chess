package client;

import chess.*;
import client.websocket.ServerMessageHandler;
import client.websocket.WebSocketFacade;
import model.GameData;
import model.create.CreateRequest;
import model.join.JoinRequest;
import model.list.GameDataModel;
import model.list.ListResult;
import model.login.LoginRequest;
import model.login.LoginResult;
import model.register.RegisterRequest;
import model.register.RegisterResult;
import exception.ResponseException;
import websocket.messages.ServerMessage;

import java.util.*;

import static ui.EscapeSequences.*;

public class Client {
    private String username = null;
    private String authToken = null;
    private HashMap<Integer, GameDataModel> gameData;
    private final ServerFacade server;
    private final String serverUrl;

    // WebSocket
    private final ServerMessageHandler serverMessageHandler;
    private WebSocketFacade ws;

    private boolean isLoggedIn = false;
    private boolean isObserver = false;
    private boolean isPlayer = false;
    private GameData game = null;

    public Client(int serverUrl, ServerMessageHandler serverMessageHandler) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = "http://localhost:" + serverUrl;
        this.serverMessageHandler = serverMessageHandler;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "create" -> create(params);
                case "join" -> join(params);
                case "list" -> list();
                case "observe" -> observe(params);
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> move(params);
                case "resign" -> resign();
                case "moves" -> moves(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String register(String... params) throws ResponseException {
        if (params.length != 3) { throw new ResponseException(400, "Expected: <username> <password> <email>"); }

        RegisterResult registerResult = server.registerUser(new RegisterRequest(params[0], params[1], params[2]));

        username = registerResult.username();
        authToken = registerResult.authToken();
        isLoggedIn = true;

        return String.format("Welcome " + SET_TEXT_BOLD + username + "!");
    }

    private String login(String... params) throws ResponseException {
        if (params.length != 2) { throw new ResponseException(400, "Expected: <username> <password>"); }

        LoginResult loginResult = server.loginUser(new LoginRequest(params[0], params[1]));

        username = loginResult.username();
        authToken = loginResult.authToken();
        isLoggedIn = true;

        return String.format("Welcome " + SET_TEXT_BOLD + username + "!");
    }

    private String create(String... params) throws ResponseException {
        if (params.length != 1) { throw new ResponseException(400, "Expected: <Name>"); }

        server.createGame(new CreateRequest(params[0]), authToken);

        return String.format("Game " + SET_TEXT_BOLD + params[0] + " has been created!");
    }

    private String list() throws ResponseException {
        ListResult listResult = server.listGames(authToken);

        gameData = new HashMap<>();
        if (listResult.games() != null) {
            StringBuilder gameList = new StringBuilder();
            gameList.append("     Game Name:    White Username:    Black Username:    \n");
            int gameNumber = 0;
            for (GameDataModel game: listResult.games()) {
                gameNumber++;
                gameData.put(gameNumber, game);
                gameList.append("(").append(gameNumber).append(")  ")
                        .append(game.gameName()).append("          ")
                        .append(game.whiteUsername()).append("               ")
                        .append(game.blackUsername())
                        .append("\n");
            }
            return gameList.toString();
        }
        else {
            return "No games found";
        }
    }

    private String join(String... params) throws ResponseException {
        if (params.length != 2) { throw new ResponseException(400, "Expected: <ID> [WHITE/BLACK]"); }

        int gameId = getGameId(params[0]);

        if (!params[1].equals("white") && !params[1].equals("black")) { throw new ResponseException(400, "Invalid team color!"); }

        ChessGame.TeamColor teamColor = (params[1].equals("white")) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        GameDataModel game = gameData.get(gameId);
        if (game == null) {
            throw new ResponseException(400, "Game does not exist!");
        }

        server.joinGame(new JoinRequest(teamColor, game.gameID()), authToken);

        ws = new WebSocketFacade(serverUrl, serverMessageHandler);
        ws.connectToGame(authToken, game.gameID());

        isPlayer = true;

        return "You have joined the game: " + SET_TEXT_BOLD + game.gameName() + "\n";
    }

    private String observe(String... params) throws ResponseException {
        if (params.length != 1) { throw new ResponseException(400, "Expected: <ID>"); }

        int gameId = getGameId(params[0]);

        GameDataModel game = gameData.get(gameId);
        if (game == null) {
            throw new ResponseException(400, "Game does not exist!");
        }

        ws = new WebSocketFacade(serverUrl, serverMessageHandler);
        ws.connectToGame(authToken, game.gameID());
        isObserver = true;

        return "You are now observing the game: " + SET_TEXT_BOLD + game.gameName() + "\n";

    }

    public String leave() throws ResponseException {
        ws.leaveGame(authToken, game.gameID());

        if (isObserver) {
            isObserver = false;
            return String.format("You have stopped observing the game: " + SET_TEXT_BOLD + game.gameName() + "\n");
        }
        else {
            isPlayer = false;
            return String.format("You have stopped playing game: " + SET_TEXT_BOLD + game.gameName() + "\n");
        }
    }

    public String move(String... params) throws ResponseException {
        if (params.length != 2) {
            throw new ResponseException(400, "Expected: <START_POSITION> <END_POSITION>");
        }

        if (params[0].length() != 2 || params[1].length() != 2) {
            throw new ResponseException(400, "Expected: <START_POSITION> <END_POSITION>");
        }

        ChessPosition startPosition = getPositionFromCoordinate(params[0]);
        ChessPiece startPiece = game.game().getBoard().getPiece(startPosition);
        ChessPosition endPosition = getPositionFromCoordinate(params[1]);

        ChessMove move;

        if (startPiece != null && startPiece.getPieceType() == ChessPiece.PieceType.PAWN && (endPosition.getRow() == 0 || endPosition.getRow() == 7)) {
            Scanner scanner = new Scanner(System.in);

            System.out.print("To what piece would you like to promote your pawn?");
            String input = scanner.nextLine().toUpperCase();

            ChessPiece.PieceType promotePieceType = switch (input) {
                case "PAWN" -> ChessPiece.PieceType.PAWN;
                case "KNIGHT" -> ChessPiece.PieceType.KNIGHT;
                case "BISHOP" -> ChessPiece.PieceType.BISHOP;
                case "ROOK" -> ChessPiece.PieceType.ROOK;
                case "KING" -> ChessPiece.PieceType.KING;
                default -> ChessPiece.PieceType.QUEEN;
            };

            move = new ChessMove(startPosition, endPosition, promotePieceType);
        }
        else {
            move = new ChessMove(startPosition, endPosition, null);
        }

        ws.makeMove(authToken, game.gameID(), move);

        return "You have successfully submitted your move.";
    }

    public ChessPosition getPositionFromCoordinate(String coordinate) throws ResponseException {
        var first = switch (coordinate.charAt(1)) {
            case '1' -> 1;
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            case '5' -> 5;
            case '6' -> 6;
            case '7' -> 7;
            case '8' -> 8;
            default -> throw new ResponseException(400, "Start and end position must be in form of row letter and column number. i.e. e4");
        };
        var second = switch (coordinate.charAt(0)) {
            case 'h' -> 8;
            case 'g' -> 7;
            case 'f' -> 6;
            case 'e' -> 5;
            case 'd' -> 4;
            case 'c' -> 3;
            case 'b' -> 2;
            case 'a' -> 1;
            default -> throw new ResponseException(400, "Start and end position must be in form of row letter and column number. i.e. e4");
        };

        return new ChessPosition(first, second);
    }

    public void notifyUser(ServerMessage message) {
        System.out.print(SET_TEXT_COLOR_BLUE + message.getMessage());
    }

    public void notifyError(ServerMessage message) {
        System.out.print(SET_TEXT_COLOR_RED + message.getMessage());
    }

    public void loadGame(ServerMessage message) {
        game = message.getGame();
        System.out.print(redraw());
    }

    private String redraw() {
        ChessGame.TeamColor team = getTeam();
        var board = game.game().getBoard();
        if (isObserver || team == ChessGame.TeamColor.WHITE) {
            return "\n" + printGame(board, ChessGame.TeamColor.WHITE);
        }
        else {
            return "\n" + printGame(board, ChessGame.TeamColor.BLACK);
        }
    }

    private String moves(String... params) throws ResponseException {
        if (params.length != 1) {
            throw new ResponseException(400, "Expected: <POSITION>");
        }

        ChessPosition position = getPositionFromCoordinate(params[0]);

        ChessPiece piece = game.game().getBoard().getPiece(position);

        ChessBoard board = game.game().getBoard();

        Collection<ChessMove> validMoves = game.game().validMoves(position);

        return buildBoardWithValidMoves(position, validMoves);
    }

    private String resign() throws ResponseException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Are you sure you want to resign? (y/n)");
        String input = scanner.nextLine().toUpperCase();

        if (input.equals("Y")) {
            ws.resign(authToken, game.gameID());
            return "Successfully submitted resignation";
        }
        else {
            return "Keep going, you got this!";
        }
    }

    private ChessGame.TeamColor getTeam() {
        if (game != null && Objects.equals(game.whiteUsername(), username)) {
            return ChessGame.TeamColor.WHITE;
        }
        else {
            return ChessGame.TeamColor.BLACK;
        }
    }

    private int getGameId(String gameString) throws ResponseException {
        int gameId;
        try {
            gameId = Integer.parseInt(gameString);
            return gameId;
        } catch (NumberFormatException e) {
            throw new ResponseException(400, "Invalid gameId: " + gameString);
        }
    }

    private String logout() throws ResponseException {
        server.logoutUser(authToken);

        username = "";
        authToken = "";
        isLoggedIn = false;

        return "Successfully logged out!";
    }

    public String help() {
        StringBuilder builder = new StringBuilder();
        if (!isLoggedIn) {
            builder.append(SET_TEXT_COLOR_MAGENTA + "    register <USERNAME> <PASSWORD> <EMAIL>" + RESET_TEXT_COLOR + " - to create an account \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    login <USERNAME> <PASSWORD>" + RESET_TEXT_COLOR + " - to play chess \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands \n");
        }
        else if (isObserver) {
            builder.append(SET_TEXT_COLOR_MAGENTA + "    redraw" + RESET_TEXT_COLOR + " - chess board \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    leave" + RESET_TEXT_COLOR + " - game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands \n");
        }
        else if (isPlayer) {
            builder.append(SET_TEXT_COLOR_MAGENTA + "    redraw" + RESET_TEXT_COLOR + " - chess board \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    leave" + RESET_TEXT_COLOR + " - game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    move <START_POSITION> <END_POSITION>" + RESET_TEXT_COLOR + " - make a move. i.e. move e4 d5 \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    resign" + RESET_TEXT_COLOR + " - accept the L \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    moves <POSITION>" + RESET_TEXT_COLOR + " - shows possible moves \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands \n");
        }
        else {
            builder.append(SET_TEXT_COLOR_MAGENTA + "    create <NAME>" + RESET_TEXT_COLOR + " - a game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    list" + RESET_TEXT_COLOR + " - games \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    join <ID> [WHITE/BLACK]" + RESET_TEXT_COLOR + " - a game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    observe <ID>" + RESET_TEXT_COLOR + " - a game \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    logout" + RESET_TEXT_COLOR + " - when you are done \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    quit" + RESET_TEXT_COLOR + " - exit program \n");
            builder.append(SET_TEXT_COLOR_MAGENTA + "    help" + RESET_TEXT_COLOR + " - possible commands \n");
        }
        return builder.toString();
    }

    private String printGame(ChessBoard game, ChessGame.TeamColor team) {
        return buildBoard(game, team);
    }

    private String buildBoard(ChessBoard game, ChessGame.TeamColor team){
        StringBuilder board = new StringBuilder();
        board.append(RESET_TEXT_COLOR);

        game.resetBoard();
        ChessPiece[][] matrix = game.chessBoard;

        if (team == ChessGame.TeamColor.BLACK) {
            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   "
                    + RESET_BG_COLOR + "\n");

            int rows = matrix.length;
            int cols = matrix[0].length;
            int rowNum = 0;
            for (int i = 0; i < rows; i++) {
                rowNum = rowNum + 1;
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR);
                for (int j = 0; j < cols; j++) {
                    ChessPiece chessPiece = matrix[i][7 - j];
                    String pieceString = getPieceString(chessPiece);
                    if ((i + j) % 2 == 0) {
                        board.append(SET_BG_COLOR_WHITE).append(pieceString).append(RESET_BG_COLOR);
                    } else {
                        board.append(SET_BG_COLOR_BLACK).append(pieceString).append(RESET_BG_COLOR);
                    }
                }
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR).append("\n");
            }

            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   "
                    + RESET_BG_COLOR + "\n");
        }
        else {
            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   "
                    + RESET_BG_COLOR + "\n");

            int rows = matrix.length;
            int cols = matrix[0].length;
            int rowNum = 9;
            for (int i = 0; i < rows; i++) {
                rowNum = rowNum - 1;
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR);
                for (int j = 0; j < cols; j++) {
                    ChessPiece chessPiece = matrix[7 - i][j];
                    String pieceString = getPieceString(chessPiece);
                    if ((i + j) % 2 == 0) {
                        board.append(SET_BG_COLOR_WHITE).append(pieceString).append(RESET_BG_COLOR);
                    } else {
                        board.append(SET_BG_COLOR_BLACK).append(pieceString).append(RESET_BG_COLOR);
                    }
                }
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR).append("\n");
            }

            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   "
                    + RESET_BG_COLOR + "\n");
        }
        return board.toString();
    }

    private String buildBoardWithValidMoves(ChessPosition position, Collection<ChessMove> moves) {
        StringBuilder board = new StringBuilder();
        board.append(RESET_TEXT_COLOR);

        ChessPiece[][] matrix = game.game().getBoard().chessBoard;

        ChessGame.TeamColor team = getTeam();

        if (team == ChessGame.TeamColor.BLACK) {
            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   "
                    + RESET_BG_COLOR + "\n");

            int rows = matrix.length;
            int cols = matrix[0].length;
            int rowNum = 0;
            for (int i = 0; i < rows; i++) {
                rowNum = rowNum + 1;
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR);
                for (int j = 0; j < cols; j++) {
                    ChessPiece chessPiece = matrix[i][7 - j];
                    String pieceString = getPieceString(chessPiece);
                    ChessPosition newPosition = new ChessPosition(i+1, 8-j);
                    if (newPosition.equals(position)) {
                        board.append(SET_BG_COLOR_YELLOW).append(pieceString).append(RESET_BG_COLOR);
                        continue;
                    }
                    if ((i + j) % 2 == 0) {
                        boolean matched = false;
                        for (ChessMove move : moves) {
                            if (newPosition.equals(move.getEndPosition())) {
                                board.append(SET_BG_COLOR_GREEN).append(pieceString).append(RESET_BG_COLOR);
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            board.append(SET_BG_COLOR_WHITE).append(pieceString).append(RESET_BG_COLOR);
                        }
                    } else {
                        boolean matched = false;
                        for (ChessMove move : moves) {
                            if (newPosition.equals(move.getEndPosition())) {
                                board.append(SET_BG_COLOR_DARK_GREEN).append(pieceString).append(RESET_BG_COLOR);
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            board.append(SET_BG_COLOR_BLACK).append(pieceString).append(RESET_BG_COLOR);
                        }
                    }
                }
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR).append("\n");
            }

            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   "
                    + RESET_BG_COLOR + "\n");
        }
        else {
            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   "
                    + RESET_BG_COLOR + "\n");

            int rows = matrix.length;
            int cols = matrix[0].length;
            int rowNum = 9;
            for (int i = 0; i < rows; i++) {
                rowNum = rowNum - 1;
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR);
                for (int j = 0; j < cols; j++) {
                    ChessPiece chessPiece = matrix[7 - i][j];
                    String pieceString = getPieceString(chessPiece);
                    ChessPosition newPosition = new ChessPosition(8 - i, j+1);
                    if (newPosition.equals(position)) {
                        board.append(SET_BG_COLOR_YELLOW).append(pieceString).append(RESET_BG_COLOR);
                        continue;
                    }
                    if ((i + j) % 2 == 0) {
                        boolean matched = false;
                        for (ChessMove move : moves) {
                            if (newPosition.equals(move.getEndPosition())) {
                                board.append(SET_BG_COLOR_GREEN).append(pieceString).append(RESET_BG_COLOR);
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            board.append(SET_BG_COLOR_WHITE).append(pieceString).append(RESET_BG_COLOR);
                        }
                    } else {
                        boolean matched = false;
                        for (ChessMove move : moves) {
                            if (newPosition.equals(move.getEndPosition())) {
                                board.append(SET_BG_COLOR_DARK_GREEN).append(pieceString).append(RESET_BG_COLOR);
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            board.append(SET_BG_COLOR_BLACK).append(pieceString).append(RESET_BG_COLOR);
                        }
                    }
                }
                board.append(SET_BG_COLOR_MAGENTA + " ").append(rowNum).append(" ").append(RESET_BG_COLOR).append("\n");
            }

            board.append(SET_BG_COLOR_MAGENTA
                    + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   "
                    + RESET_BG_COLOR + "\n");
        }
        return board.toString();
    }

    private String getPieceString(ChessPiece chessPiece) {
        if (chessPiece == null) {
            return "   ";
        }

        if (chessPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return getString(chessPiece, SET_TEXT_COLOR_DARK_GREY + BLACK_KING + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_DARK_GREY + BLACK_QUEEN + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_DARK_GREY + BLACK_ROOK + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_DARK_GREY + BLACK_BISHOP + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_DARK_GREY + BLACK_KNIGHT + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_DARK_GREY + BLACK_PAWN + RESET_TEXT_COLOR);
        }
        else {
            return getString(chessPiece, WHITE_KING, WHITE_QUEEN, WHITE_ROOK, WHITE_BISHOP, WHITE_KNIGHT, WHITE_PAWN);
        }
    }

    private String getString(ChessPiece chessPiece, String king, String queen, String rook, String bishop, String knight, String pawn) {
        return switch (chessPiece.getPieceType()) {
            case KING -> king;
            case QUEEN -> queen;
            case ROOK -> rook;
            case BISHOP -> bishop;
            case KNIGHT -> knight;
            case PAWN -> pawn;
        };
    }
}