package rule;

import game.GamePhase;
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
public class PlayingRuleChecker {
    /**
     * 验证玩家是否可以出牌。
     *
     * 该方法用于检查当前游戏房间状态和阶段，以确定指定玩家是否可以进行出牌操作。
     * 目前仅检查房间是否为空以及当前阶段是否为PLAYING。后续版本将增加更多合法性校验。
     *
     * @param room 游戏房间对象，不能为空
     * @param playerId 玩家ID，必须是房间中的有效玩家
     * @param cards 出牌的牌组列表，不能为空
     * @throws IllegalStateException 如果房间为空或当前阶段不是PLAYING
     */
    public static void validateCanPlay(GameRoom room, int playerId, List<Integer> cards) {
        if (room == null){
            throw new IllegalStateException("房间不能为空");
        }
        if (GamePhase.PLAYING != room.getCurrentPhase()){
            throw new IllegalStateException("只能为PLAYING阶段");
        }
    }
    public static void identify(){

    }
}
