package server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class DealResult {
    private final List<PlayerHand> playerHands;
    private final TreeSet<Integer> holeCards;

    public DealResult(List<PlayerHand> playerHands, TreeSet<Integer> holeCards) {
        this.playerHands = Collections.unmodifiableList(new ArrayList<>(playerHands));
        this.holeCards = new TreeSet<>(holeCards);
    }

    public List<PlayerHand> getPlayerHands() {
        return playerHands;
    }

    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }
}
