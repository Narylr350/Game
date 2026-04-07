package game.action;

import java.util.List;

/**
 * 游戏动作类。
 * <p>
 * 封装玩家发起的动作请求，包含玩家ID、动作类型和要打出的牌。
 * 该类为不可变对象，创建后无法修改。
 * </p>
 */
public class GameAction {
    private final int playerId;
    private final ActionType type;
    private final List<Integer> cards;

    /**
     * 创建游戏动作对象。
     *
     * @param playerId 发起动作的玩家ID
     * @param type 动作类型
     * @param cards 要打出的牌列表
     */
    public GameAction(int playerId, ActionType type, List<Integer> cards) {
        this.playerId = playerId;
        this.type = type;
        this.cards = cards;
    }

    /**
     * 获取发起动作的玩家ID。
     *
     * @return 玩家ID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * 获取动作类型。
     *
     * @return 动作类型枚举
     */
    public ActionType getType() {
        return type;
    }

    /**
     * 获取要打出的牌列表。
     *
     * @return 牌列表
     */
    public List<Integer> getCards() {
        return cards;
    }
}