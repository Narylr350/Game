package game.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 玩家提交给游戏流程的一次动作。
 * <p>
 * 该对象创建后不可变，避免外部继续修改牌列表影响规则判断。
 * </p>
 */
public class GameAction {
    private final int playerId;                    // 玩家ID
    private final ActionType type;                 // 动作类型
    private final List<Integer> cards;             // 要打出的牌

    public GameAction(int playerId, ActionType type, List<Integer> cards) {
        this.playerId = playerId;
        this.type = type;
        this.cards = cards == null ? List.of() : Collections.unmodifiableList(new ArrayList<>(cards));
    }

    public int getPlayerId() {
        return playerId;
    }

    public ActionType getType() {
        return type;
    }

    public List<Integer> getCards() {
        return cards;
    }
}
