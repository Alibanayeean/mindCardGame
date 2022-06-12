package api.utils;

import api.dataeggs.joinablegames.JoinableGamesEgg;
import backend.logic.games.Game;
import backend.logic.games.GameManager;

import java.util.Map;

public class JoinableGamesUtils {
    public static JoinableGamesEgg getJoinableGamesDataEgg() {
        JoinableGamesEgg dataEgg = new JoinableGamesEgg();

        Map<Integer, Game> gamesMap = GameManager.getGamesMap();
        for (Game game : gamesMap.values()) {
            dataEgg.addJoinableGame(game);
        }

        return dataEgg;
    }
}