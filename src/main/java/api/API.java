package api;

import api.dataeggs.MakingMoveEgg;
import api.dataeggs.NewGameEgg;
import api.dataeggs.gamestate.Emoji;
import api.dataeggs.gamestate.GameStateEgg;
import api.dataeggs.joinablegames.JoinableGamesEgg;
import api.utils.*;
import backend.logic.games.Game;
import backend.logic.games.GameManager;
import backend.logic.games.components.ninjahandling.NinjaHandler;
import backend.logic.models.players.Human;

public class API {
    public static int getHostId(int gameId) {
        Game game = GameManager.getGameById(gameId);
        return game.getHostHumanId();
    }

    public static void setEmoji(int gameId, int playerId, String emojiString) {
        Emoji emoji = GameStateUtils.stringToEnum(emojiString);

        Game game = GameManager.getGameById(gameId);
        GameStateUtils.setEmojiById(game, playerId, emoji);
    }

    public static int addNewPlayerToLobby() {
        // returning Id
        Human human = GameManager.createNewHumanInLobby();
        return human.getPlayerId();
    }

    public static String addNewGame(int numberOfBots, int currentHumanId) {
        // returning Gson -> can make game or not // gameWasSuccessfullyCreated, gameId
        Game game = new Game(numberOfBots, currentHumanId);
        NewGameEgg dataEgg = new NewGameEgg(true, game.getGameId());
        GameManager.addGame(game);
        return GsonUtils.getJsonString(dataEgg);
    }

    public static synchronized boolean joinGame(int gameId, int playerId) {
        // shouldn't have playerId
        // returns: can join game or not
        Game game = GameManager.getGameById(gameId);
        boolean canJoinGame = JoinableGamesUtils.gameIsJoinable(game);
        if (canJoinGame) {
            GameManager.joinGame(gameId, playerId);
        }

        return canJoinGame;
    }

    public static String getAllJoinableGames() {
        // returning Gson -> gameId, number of free players, number of bots
        // if num of free = 0 => don't return
        JoinableGamesEgg dataEgg = JoinableGamesUtils.getJoinableGamesDataEgg();

        return GsonUtils.getJsonString(dataEgg);
    }

    public static boolean gameHasStarted(int gameId) {
        Game game = GameManager.getGameById(gameId);
        return game.gameHasBeenStarted();
    }

    public static String getUpdatedGameState(int gameId, int currentHumanId) {
        // updates game and returns Gson of GameState ->
        // actionHasCausedLoss, smallestLossyCardNumber, hostId, isGameStarted, has there any ninja request, level, num of hearts, last card game on ground, num of players, number of cards on ground, hands (my player) , opponents hand meta data (sorted by id)
        //                                                                                                              exm:  60, 17 ,40 ,2 , 0 : 6 , 1 : 7 , 3 : 2

        Game game = GameManager.getGameById(gameId);
        GameStateEgg dataEgg = new GameStateEgg(game, currentHumanId);

        game.getActionLogger().setLatestActionHasCausedLoss(false);

        return GsonUtils.getJsonString(dataEgg);
    }

    public static void showedSmallestCards(int gameId) {
        Game game = GameManager.getGameById(gameId);
        NinjaHandler handler = game.getNinjaHandler();
        handler.setShouldShowSmallestCards(false); // has just seen the revealed cards. so there's no need for displaying them
    }


    public static synchronized String makeMove(int gameId, int playerId, int cardIndex) {
        // moveWasValid, doesMoveCauseLossOfHeart, smallestCardThatHasCausedLoss (if the second boolean is true)
        Game game = GameManager.getGameById(gameId);
        boolean moveRespectsGroundOrder = MakingMoveUtils.moveRespectsGroundOrder(game, playerId);
        boolean moveCausesLossOfHealth = MakingMoveUtils.moveCausesHealthLoss(game, playerId);
        int smallestCardNumberThatHasCausedLoss = MakingMoveUtils.getSmallestCardInPlayersHands(game);

        // making a move in GameManager:
        MakingMoveUtils.dropCardInGameManager(game, playerId, cardIndex);

        // returning the data-egg:
        MakingMoveEgg dataEgg = new MakingMoveEgg(moveRespectsGroundOrder, moveCausesLossOfHealth,
                smallestCardNumberThatHasCausedLoss);
        return GsonUtils.getJsonString(dataEgg);
    }

    public static boolean startGame(int gameId) {
        // return a boolean that can or not
        return GameManager.startGameById(gameId);
    }

    public static void disconnectHuman(int playerId) {
        if (playerId == -1) {
            return;
        }

        int gameId = GameManager.getGameIdWithPlayerInside(playerId);
        GameManager.disconnectHumanFromGame(gameId, playerId);
    }

    public static synchronized boolean castNinjaVote(boolean agreesWithRequest, int playerId, int gameId) {
        Human human = NinjaRequestUtils.getHumanById(gameId, playerId);
        GameManager.castVoteForNinjaRequestInGame(gameId, human, agreesWithRequest);

        boolean ninjaRequestHasBeenCompleted = NinjaRequestUtils.ninjaRequestHasBeenCompleted(gameId);
        if (ninjaRequestHasBeenCompleted) {
            GameManager.dropNinjaCardInGame(gameId);
        }

        return NinjaRequestUtils.ninjaRequestHasBeenCompleted(gameId);
    }
}