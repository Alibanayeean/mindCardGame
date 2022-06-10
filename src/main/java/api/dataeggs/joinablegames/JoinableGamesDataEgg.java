package api.dataeggs.joinablegames;

import backend.logic.games.Game;

import java.util.ArrayList;
import java.util.List;

public class JoinableGamesDataEgg {
    private List<JoinableGame> joinableGamesList;

    public JoinableGamesDataEgg() {
        joinableGamesList = new ArrayList<JoinableGame>();
    }

    public void addJoinableGame(Game game) {
        int gameId = game.getGameId();
        int numberOfBots = game.getNumberOfBots();
        int numberOfFreePlayers = 4 - game.getNumberOfPlayers();
        JoinableGame joinableGame = new JoinableGame(gameId, numberOfFreePlayers, numberOfBots);

        joinableGamesList.add(joinableGame);
    }
}
