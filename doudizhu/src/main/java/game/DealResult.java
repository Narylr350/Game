package game;

import java.util.List;
import java.util.TreeSet;

// 发牌后的临时结果对象：把玩家手牌和底牌一起交给上层组装房间。
public class DealResult {
    private final List<PlayerState> players;
    private final TreeSet<Integer> holeCards;

    public DealResult(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = List.copyOf(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    public List<PlayerState> getPlayers() {
        return players;
    }

    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }
}
