import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    private String sendRequest(String endpoint, String method, String body) throws IOException {
        HttpURLConnection connection = getHttpURLConnection(endpoint, method, body);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            throw new IOException("Request failed with response code: " + responseCode);
        }
    }

    private HttpURLConnection getHttpURLConnection(String endpoint, String method, String body) throws IOException {
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        if (body != null && !body.isEmpty()) {
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
        }
        return connection;
    }

    public String registerUser(String jsonRequest) throws IOException {
        return sendRequest("/user", "POST", jsonRequest);
    }

    public String loginUser(String jsonRequest) throws IOException {
        return sendRequest("/session", "POST", jsonRequest);
    }

    public String logoutUser() throws IOException {
        return sendRequest("/session", "DELETE", null);
    }

    public String clearDatabase() throws IOException {
        return sendRequest("/db", "DELETE", null);
    }

    public String listGames() throws IOException {
        return sendRequest("/game", "GET", null);
    }

    public String createGame(String jsonRequest) throws IOException {
        return sendRequest("/game", "POST", jsonRequest);
    }

    public String joinGame(String jsonRequest) throws IOException {
        return sendRequest("/game", "PUT", jsonRequest);
    }
}