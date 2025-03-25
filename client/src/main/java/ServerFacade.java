package server;

import com.google.gson.Gson;
import exception.ErrorResponse;
import exception.ResponseException;
import model.Pet;
import service.create.CreateRequest;
import service.create.CreateResult;
import service.join.JoinRequest;
import service.list.ListResult;
import service.login.LoginRequest;
import service.login.LoginResult;
import service.register.RegisterRequest;
import service.register.RegisterResult;

import java.io.*;
import java.net.*;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }


    public RegisterResult registerUser(RegisterRequest registerRequest) throws server.ResponseException {
        return makeRequest("/user", "POST", registerRequest, RegisterResult.class, null);
    }

    public LoginResult loginUser(LoginRequest loginRequest) throws server.ResponseException {
        return makeRequest("/session", "POST", loginRequest, LoginResult.class, null);
    }

    public void logoutUser(String authToken) throws server.ResponseException {
        makeRequest("/session", "DELETE", null, null, authToken);
    }

    public void clearDatabase(String authToken) throws server.ResponseException {
        makeRequest("/db", "DELETE", null, null, authToken);
    }

    public ListResult listGames(String authToken) throws server.ResponseException {
        return makeRequest("/game", "GET", null, ListResult.class, authToken);
    }

    public CreateResult createGame(CreateRequest createRequest, String authToken) throws server.ResponseException {
        return makeRequest("/game", "POST", createRequest, CreateResult.class, authToken);
    }

    public void joinGame(JoinRequest joinRequest, String authToken) throws server.ResponseException {
        makeRequest("/game", "PUT", joinRequest, null, authToken);
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws server.ResponseException {
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
        } catch (server.ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new server.ResponseException(500, ex.getMessage());
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

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, server.ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw server.ResponseException.fromJson(respErr);
                }
            }

            throw new server.ResponseException(status, "other failure: " + status);
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
