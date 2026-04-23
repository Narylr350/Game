package game.handler;

import game.action.ActionType;
import game.action.GameAction;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.PlayerState;
import game.state.PlayingState;
import rule.play.PlayCheckResult;
import rule.play.PlayingRuleChecker;

import java.util.List;

/**
 * 出牌阶段处理器。
 * <p>
 * 负责把出牌规则检查结果映射成对客户端可见的 GameResult。
 * </p>
 */
public class PlayingHandler {
    /**
     * 处理出牌阶段的玩家动作。
     *
     * @param room 游戏房间对象
     * @param action 玩家动作
     * @return 处理结果
     */
    public GameResult handle(GameRoom room, GameAction action) {
        PlayingState playingState = room.getPlayingState();
        final int playerId = action.getPlayerId();
        int currentPlayerId = room.getCurrentPlayerId();
        ActionType actionType = action.getType();
        List<Integer> cards = action.getCards();
        PlayerState currentPlayer = room.getPlayerById(currentPlayerId);
        PlayCheckResult playCheckResult = PlayingRuleChecker.checkPlay(room, cards);

        if (currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就出", playerId);
        }
        // 出牌阶段的业务非法状态统一返回 rejected，而不是依赖异常让服务端兜底。
        if (playCheckResult == PlayCheckResult.WRONG_PHASE) {
            return GameResult.rejected("当前阶段不能出牌", playerId);
        }
        if (playCheckResult == PlayCheckResult.INVALID_CARD_PATTERN) {
            return GameResult.rejected("瞎几把出什么呢", playerId);
        }
        if (playCheckResult == PlayCheckResult.CARD_TYPE_MISMATCH) {
            return GameResult.rejected("牌型和上家不匹配", playerId);
        }
        if (playCheckResult == PlayCheckResult.NOT_STRONGER_THAN_LAST) {
            return GameResult.rejected("没有大过上家", playerId);
        }

        if (playCheckResult == PlayCheckResult.VALID) {
            // 出牌
            if (ActionType.PLAY_CARD == actionType) {
                boolean removed = currentPlayer.removeCards(cards);
                if (!removed) {
                    return GameResult.rejected(playerId + "使用了无中生有", playerId);
                }
                playingState.setLastPlayedCards(cards);
                //playingState.setHighestCardPlayerId(playerId);
                currentPlayer.removeCards(cards);
                return GameResult.accepted("出牌");
            }
            // 不出
            if (ActionType.PASS_CARD == actionType) {
                playingState.addPassCount();
                room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
                return GameResult.accepted("不出");
            }
        }
        return GameResult.rejected("当前操作无效", playerId);
    }
}
