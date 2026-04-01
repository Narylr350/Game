package game;

import java.util.TreeSet;

public class PlayerState {
    private final int playerId;
    private final String playerName;
    private final TreeSet<Integer> cards;

    public PlayerState(int playerId, String playerName, TreeSet<Integer> cards) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.cards = new TreeSet<>(cards);
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public TreeSet<Integer> getCards() {
        return new TreeSet<>(cards);
    }
}
