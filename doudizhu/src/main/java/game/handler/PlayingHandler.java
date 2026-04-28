package game.handler;

import game.action.ActionType;
import game.action.GameAction;
import game.enumtype.GamePhase;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.PlayerState;
import game.state.PlayingState;
import rule.play.PlayCheckResult;
import rule.play.PlayingRuleChecker;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        final int currentPlayerId = room.getCurrentPlayerId();
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
                playingState.setRecentPlayedCards(cards);
                playingState.setLastPlayedCards(cards);
                boolean removed = currentPlayer.removeCards(cards);
                if (!removed) {
                    return GameResult.rejected(playerId + "使用了无中生有", playerId);
                }
                playingState.setHighestCardPlayerId(playerId);
                if (currentPlayer.getCards().isEmpty()) {
                    return settleGame(room, playerId);
                }
                room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
                playingState.resetPassCount();
                return GameResult.accepted("出牌");
            }
            // 不出
            if (ActionType.PASS_CARD == actionType) {
                if (playingState.getHighestCardPlayerId() == currentPlayerId){
                    return GameResult.rejected("该你出了",playerId);
                }
                playingState.incrementPassCount();
                if (playingState.getPassCount() == 2){
                    room.setCurrentPlayerId(playingState.getHighestCardPlayerId());
                    playingState.resetRound();
                    return GameResult.accepted("不出");
                }
                room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
                return GameResult.accepted("不出");
            }
        }
        return GameResult.rejected("当前操作无效", playerId);
    }

    private GameResult settleGame(GameRoom room, int winnerId) {
        room.setCurrentPhase(GamePhase.SETTLE);

        Integer landlordPlayerId = room.getLandlordPlayerId();
        boolean landlordWin = landlordPlayerId != null && landlordPlayerId == winnerId;
        Map<Integer, String> playerMessages = new LinkedHashMap<>();

        for (PlayerState player : room.getPlayers()) {
            if (landlordPlayerId != null && player.getPlayerId() == landlordPlayerId) {
                playerMessages.put(player.getPlayerId(), landlordWin ? "地主胜利" : "地主失败");
            } else {
                playerMessages.put(player.getPlayerId(), landlordWin ? "农民失败" : "农民胜利");
            }
        }

        return GameResult.gameSettled("游戏结束，玩家 " + winnerId + " 出完手牌", playerMessages, winnerId);
    }
}
