package server.service;

import server.model.GameSession;

import java.util.List;

public class GameSessionManager {
    private final GameService gameService;
    private GameSession currentSession;

    public GameSessionManager() {
        this.gameService = new GameService();
    }

    public GameSession startGame(List<String> playerNames) {
        currentSession = gameService.startGame(playerNames);
        return currentSession;
    }

    public GameSession getCurrentSession() {
        return currentSession;
    }
}
