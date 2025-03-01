package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class AuthMemoryDataAccess implements AuthDAO {
    final private HashMap<String, AuthData> auths = new HashMap<>();

    public void clear() {
        auths.clear();
    }

    public AuthData getAuth(String authToken) {
        return auths.get(authToken);
    }

    public void deleteAuth(String authToken) {
        auths.remove(authToken);
    }

    public void createAuth(AuthData authData) {
        auths.put(authData.authToken(), authData);
    }
}
