package game;

import java.util.List;
import java.util.TreeSet;

public class DealResult {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;

    protected DealResult(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = List.copyOf(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    protected List<PlayerState> getPlayers() {
        return players;
    }

    protected TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }
}
