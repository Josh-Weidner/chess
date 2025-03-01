package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class AuthMemoryDataAccess {
    final private HashMap<String, AuthData> auths = new HashMap<>();

    public void clear() {
        auths.clear();
    }
}
