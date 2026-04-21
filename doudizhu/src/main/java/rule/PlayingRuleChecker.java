package rule;

import game.GamePhase;
import game.GameRoom;
import game.state.PlayingState;

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
     * 检查玩家的出牌是否符合当前游戏规则。
     *
     * @param room 游戏房间对象，不能为空
     * @param cards 玩家要出的牌列表
     * @return 返回一个PlayCheckResult枚举值，表示出牌检查的结果：
     *         - VALID：当前出牌有效
     *         - INVALID_CARD_PATTERN：出牌模式无效
     *         - CARD_TYPE_MISMATCH：牌型不匹配
     *         - NOT_STRONGER_THAN_LAST：当前出牌没有上一手大
     */
    public static PlayCheckResult checkPlay(GameRoom room, List<Integer> cards) {
        if (room == null) {
            throw new IllegalStateException("房间不能为空");
        }
        if (GamePhase.PLAYING != room.getCurrentPhase()) {
            throw new IllegalStateException("只能为PLAYING阶段");
        }
        if (PlayCardGroup.analyzeCards(cards)
                .getType() == CardType.INVALID) {
            return PlayCheckResult.INVALID_CARD_PATTERN;
        }
        if (hasLastPlay(room)){
            return canBeat(room,cards);
        }
        return PlayCheckResult.VALID;
    }

    /**
     * 检查房间中是否已有上一手出牌记录。
     *
     * @param room 游戏房间对象，不能为空
     * @return 如果存在上一手出牌记录，则返回true；否则返回false
     */
    private static boolean hasLastPlay(GameRoom room) {
        PlayingState playingState = room.getPlayingState();
        return playingState.getLastPlayedCards() != null;
    }

    /**
     * 检查当前出牌是否可以压过上家的牌。
     *
     * @param room 游戏房间对象，包含当前游戏状态信息
     * @param currentCards 当前玩家要出的牌列表
     * @return 返回一个PlayCheckResult枚举值，表示出牌检查的结果：
     *         - VALID：当前出牌有效且可以压过上家
     *         - CARD_TYPE_MISMATCH：当前出牌类型与上家出牌类型不匹配
     *         - NOT_STRONGER_THAN_LAST：当前出牌不足以压过上家
     */
    private static PlayCheckResult canBeat(GameRoom room, List<Integer> currentCards) {
        PlayingState playingState = room.getPlayingState();
        List<Integer> lastPlayedCards = playingState.getLastPlayedCards();
        PlayCardGroup currentCardGroup = PlayCardGroup.analyzeCards(currentCards);
        PlayCardGroup lastCardGroup = PlayCardGroup.analyzeCards(lastPlayedCards);
        //判断牌型是否一致
        if (currentCardGroup.getType()!=lastCardGroup.getType()){
            return PlayCheckResult.CARD_TYPE_MISMATCH;
        }
        //判断牌是否大过上家
        if (currentCardGroup.getMainRank() < lastCardGroup.getMainRank()){
            return PlayCheckResult.NOT_STRONGER_THAN_LAST;
        }
        return PlayCheckResult.VALID;
    }
}
