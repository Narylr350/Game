package game.handler;

import game.action.ActionType;
import game.action.GameAction;
import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.LandlordState;
import rule.landlord.LandlordCheckResult;
import rule.landlord.LandlordRuleChecker;

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
        LandlordState landlordState = room.getLandlordState();
        final int playerId = action.getPlayerId();
        final int currentPlayerId = room.getCurrentPlayerId();
        ActionType actionType = action.getType();

        LandlordCheckResult landlordCheckResult = LandlordRuleChecker.validateCanCallLandlord(room);

        // 地主阶段的业务非法状态统一返回 rejected，而不是依赖异常让服务端兜底。
        if (landlordCheckResult == LandlordCheckResult.WRONG_PHASE) {
            return GameResult.rejected("当前阶段不能叫地主", playerId);
        }

        if (landlordCheckResult == LandlordCheckResult.LANDLORD_ALREADY_DECIDED) {
            return GameResult.rejected("地主已确定", playerId);
        }

        if (actionType == null) {
            return GameResult.rejected("你在干赣神魔", playerId);
        }

        if (currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就抢", playerId);
        }

        // 叫地主
        if (ActionType.CALL == actionType) {
            landlordState.setLandlordCandidateId(currentPlayerId);

            // 前两家都不叫，最后一家叫，直接确认地主
            if (landlordState.getCallPassCount() == 2) {
                gameFlow.confirmLandlord(room, currentPlayerId);
                return GameResult.landlordDecided("地主确认：玩家 " + currentPlayerId);
            }

            landlordState.setFirstCallerId(currentPlayerId);
            room.setCurrentPhase(GamePhase.ROB_LANDLORD);
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            return GameResult.accepted("叫地主");
        }

        // 不叫
        if (ActionType.PASS == actionType) {
            landlordState.addCallPassPlayerId(currentPlayerId);
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            landlordState.incrementCallPassCount();

            // 三人都不叫，重开
            if (landlordState.getCallPassCount() == 3) {
                landlordState.resetLandlordPhaseState();
                room.setCurrentPhase(GamePhase.DEALING);
                return GameResult.redealRequired("三人都不叫地主，重新发牌");
            }

            return GameResult.accepted("不叫地主");
        }

        return GameResult.rejected("当前操作无效", playerId);
    }
}
