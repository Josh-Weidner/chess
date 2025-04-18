package client;

import com.google.gson.Gson;
import model.create.CreateRequest;
import model.create.CreateResult;
import model.join.JoinRequest;
import model.list.ListResult;
import model.login.LoginRequest;
import model.login.LoginResult;
import model.register.RegisterRequest;
import model.register.RegisterResult;
import exception.ResponseException;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }


    public RegisterResult registerUser(RegisterRequest registerRequest) throws ResponseException {
        return makeRequest("/user", "POST", registerRequest, RegisterResult.class, null);
    }

    public LoginResult loginUser(LoginRequest loginRequest) throws ResponseException {
        return makeRequest("/session", "POST", loginRequest, LoginResult.class, null);
    }

    public void logoutUser(String authToken) throws ResponseException {
        makeRequest("/session", "DELETE", null, null, authToken);
    }

    public void clearDatabase() throws ResponseException {
        makeRequest("/db", "DELETE", null, null, null);
    }

    public ListResult listGames(String authToken) throws ResponseException {
        return makeRequest("/game", "GET", null, ListResult.class, authToken);
    }

    public CreateResult createGame(CreateRequest createRequest, String authToken) throws ResponseException {
        return makeRequest("/game", "POST", createRequest, CreateResult.class, authToken);
    }

    public void joinGame(JoinRequest joinRequest, String authToken) throws ResponseException {
        makeRequest("/game", "PUT", joinRequest, null, authToken);
    }

    private <T> T makeRequest(String path, String method,  Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            if (authToken != null && !authToken.isEmpty()) {
                http.setRequestProperty("Authorization", authToken);
            }

            writeBody(request, http);
            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}
