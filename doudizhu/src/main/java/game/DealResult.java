package game;

import game.state.PlayerState;

import java.util.List;
import java.util.TreeSet;

/**
 * 发牌结果类。
 * <p>
 * 封装发牌操作的结果,包含所有玩家的状态(手牌)和底牌信息。
 * 该类为不可变对象,创建后无法修改。
 * </p>
 */
public class DealResult {
    private final List<PlayerState> players;     // 所有玩家状态
    private final TreeSet<Integer> holeCards;    // 底牌

    /**
     * 创建发牌结果对象。
     *
     * @param players   所有玩家的状态列表
     * @param holeCards 底牌集合
     */
    public DealResult(List<PlayerState> players, TreeSet<Integer> holeCards) {
        this.players = List.copyOf(players);
        this.holeCards = new TreeSet<>(holeCards);
    }

    /**
     * 获取所有玩家状态的只读列表。
     *
     * @return 包含所有玩家状态的不可变列表
     */
    public List<PlayerState> getPlayers() {
        return players;
    }

    /**
     * 获取底牌的副本。
     *
     * @return 底牌集合的副本(已排序)
     */
    public TreeSet<Integer> getHoleCards() {
        return new TreeSet<>(holeCards);
    }
}
