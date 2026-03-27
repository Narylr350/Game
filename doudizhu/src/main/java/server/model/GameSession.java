package server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class GameSession {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;
    private Integer landlordPlayerId;
    private Integer currentTurnPlayerId;
    private boolean gameStarted;
    private boolean gameFinished;

    public GameSession(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }

    public Integer getLandlordPlayerId() {
        return landlordPlayerId;
    }

    public void setLandlordPlayerId(Integer landlordPlayerId) {
        this.landlordPlayerId = landlordPlayerId;
    }

    public Integer getCurrentTurnPlayerId() {
        return currentTurnPlayerId;
    }

    public void setCurrentTurnPlayerId(Integer currentTurnPlayerId) {
        this.currentTurnPlayerId = currentTurnPlayerId;
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public void setGameStarted(boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public boolean isGameFinished() {
        return gameFinished;
    }

    public void setGameFinished(boolean gameFinished) {
        this.gameFinished = gameFinished;
    }

    public PlayerState findPlayerById(int playerId) {
        for (PlayerState player : players) {
            if (player.getPlayerId() == playerId) {
                return player;
            }
        }
        return null;
    }
}
