package game.handler;

import game.GameFlow;
import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import rule.LandlordRule;

/**
 * 叫地主阶段处理器。
 * <p>
 * 负责处理玩家在叫地主阶段的操作，包括叫地主和不叫两种选择。
 * </p>
 */
public class CallLandlordHandler {

    private final GameFlow gameFlow;

    public CallLandlordHandler(GameFlow gameFlow) {
        this.gameFlow = gameFlow;
    }

    /**
     * 处理叫地主阶段的玩家动作。
     *
     * @param room 游戏房间对象
     * @param action 玩家动作
     * @return 处理结果
     */
    public GameResult handle(GameRoom room, GameAction action) {
        Integer currentPlayerId = room.getCurrentPlayerId();
        int playerId = action.getPlayerId();
        ActionType actionType = action.getType();

        if (!LandlordRule.canCallLandlord(room)) {
            return GameResult.rejected("不能叫地主");
        }

        if (actionType == null) {
            return GameResult.rejected("你在干赣神魔", playerId);
        }

        if (currentPlayerId == null || currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就抢", playerId);
        }

        // 叫地主
        if (ActionType.CALL == actionType) {
            room.setLandlordCandidateId(currentPlayerId);

            // 前两家都不叫，最后一家叫，直接确认地主
            if (room.getCallPassCount() == 2) {
                gameFlow.confirmLandlord(room, currentPlayerId);
                return GameResult.landlordDecided("地主确认：玩家 " + currentPlayerId);
            }

            room.setFirstCallerId(currentPlayerId);
            room.setCurrentPhase(GamePhase.ROB_LANDLORD);
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            return GameResult.accepted("叫地主");
        }

        // 不叫
        if (ActionType.PASS == actionType) {
            room.addCallPassPlayerId(currentPlayerId);
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            room.incrementCallPassCount();

            // 三人都不叫，重开
            if (room.getCallPassCount() == 3) {
                room.resetLandlordPhaseState();
                room.setCurrentPhase(GamePhase.DEALING);
                return GameResult.redealRequired("三人都不叫地主，重新发牌");
            }

            return GameResult.accepted("不叫地主");
        }

        return GameResult.rejected("当前操作无效", playerId);
    }
}