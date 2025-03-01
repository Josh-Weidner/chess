package dataaccess;

import model.GameData;

import java.util.HashMap;

public class GameMemoryDataAccess {
    final private HashMap<Integer, GameData> games = new HashMap<Integer, GameData>();

    public void clear() {
        games.clear();
    }
}
