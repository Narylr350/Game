package rule;

import game.GameRoom;

import java.util.List;

/**
 * 出牌规则检查器。
 * <p>
 * 负责判断玩家的出牌操作是否合法。
 * </p>
 * <p>
 * <b>注意:</b> 第3阶段会把真正的出牌合法性判断补进来,当前只做最小防御性校验。
 * 目前仅检查房间、玩家、牌组是否为空等基础条件,不检查牌型合法性。
 * </p>
 */
public class PlayRuleChecker {
    /**
     * 判断玩家是否可以出指定的牌。
     * <p>
     * 当前阶段的校验规则:
     * <ul>
     *   <li>房间对象不能为空</li>
     *   <li>玩家必须在房间内(能通过playerId找到)</li>
     *   <li>要打出的牌组不能为空</li>
     * </ul>
     * </p>
     *
     * @param room 游戏房间对象
     * @param playerId 要出牌的玩家ID
     * @param cards 要打出的牌组
     * @return 如果满足所有基础条件则返回true,否则返回false
     */
    public boolean canPlay(GameRoom room, int playerId, List<Integer> cards) {
        return room != null
                && room.getPlayerById(playerId) != null
                && cards != null
                && !cards.isEmpty();
    }
}
