package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import client.create.CreateRequest;
import client.join.JoinRequest;
import client.list.GameDataModel;
import client.list.ListResult;
import client.login.LoginRequest;
import client.login.LoginResult;
import client.register.RegisterRequest;
import client.register.RegisterResult;

import java.util.Arrays;
import java.util.HashMap;

import static ui.EscapeSequences.*;

public class Client {
    private String username = null;
    private String authToken = null;
    private HashMap<Integer, GameDataModel> gameData;
    private final ServerFacade server;
    private boolean isLoggedIn = false;

    public Client(int serverUrl) {
        server = new ServerFacade(serverUrl);
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

        return "You have joined the game: " + SET_TEXT_BOLD + game.gameName() + "\n" +
                printGame(new ChessBoard(), teamColor);
    }

    private String observe(String... params) throws ResponseException {
        if (params.length != 1) { throw new ResponseException(400, "Expected: <ID>"); }

        int gameId = getGameId(params[0]);

        GameDataModel game = gameData.get(gameId);
        if (game == null) {
            throw new ResponseException(400, "Game does not exist!");
        }

        return "You are now observing the game: " + SET_TEXT_BOLD + game.gameName() + "\n" +
                printGame(new ChessBoard(), ChessGame.TeamColor.WHITE);
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
            // First row
            board.append(SET_BG_COLOR_MAGENTA + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   " + RESET_BG_COLOR + "\n");

            int rows = matrix.length;
            int cols = matrix[0].length;
            int rowNum = 1;
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

            // Last row
            board.append(SET_BG_COLOR_MAGENTA + "   " + " h " + " g " + " f " + " e " + " d " + " c " + " b " + " a " + "   " + RESET_BG_COLOR + "\n");
        }
        else {
            // First row
            board.append(SET_BG_COLOR_MAGENTA + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   " + RESET_BG_COLOR + "\n");

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

            // Last row
            board.append(SET_BG_COLOR_MAGENTA + "   " + " a " + " b " + " c " + " d " + " e " + " f " + " g " + " h " + "   " + RESET_BG_COLOR + "\n");
        }
        return board.toString();
    }

    private String getPieceString(ChessPiece chessPiece) {
        if (chessPiece == null) {
            return "   ";
        }

        if (chessPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            return getString(chessPiece, SET_TEXT_COLOR_DARK_GREY + BLACK_KING + RESET_TEXT_COLOR, SET_TEXT_COLOR_DARK_GREY + BLACK_QUEEN + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_DARK_GREY + BLACK_ROOK + RESET_TEXT_COLOR, SET_TEXT_COLOR_DARK_GREY + BLACK_BISHOP + RESET_TEXT_COLOR,
                    SET_TEXT_COLOR_DARK_GREY + BLACK_KNIGHT + RESET_TEXT_COLOR, SET_TEXT_COLOR_DARK_GREY + BLACK_PAWN + RESET_TEXT_COLOR);
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