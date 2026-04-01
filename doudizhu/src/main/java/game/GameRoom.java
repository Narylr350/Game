package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

public class GameRoom {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;

    public GameRoom(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = new ArrayList<>(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    public List<PlayerState> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
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
