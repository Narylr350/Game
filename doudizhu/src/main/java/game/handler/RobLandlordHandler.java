package game.handler;

import game.GameFlow;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import rule.LandlordCheckResult;
import rule.LandlordRuleChecker;
import game.state.LandlordState;

import java.util.List;

/**
 * 抢地主阶段处理器。
 * <p>
 * 负责处理玩家在抢地主阶段的操作。叫地主阶段已经选择不叫的玩家，在本轮会被强制PASS。
 * </p>
 */
public class RobLandlordHandler {

    private final GameFlow gameFlow;

    public RobLandlordHandler(GameFlow gameFlow) {
        this.gameFlow = gameFlow;
    }

    /**
     * 处理抢地主阶段的玩家动作。
     *
     * @param room   游戏房间对象
     * @param action 玩家动作
     * @return 处理结果
     */
    public GameResult handle(GameRoom room, GameAction action) {
        LandlordState landlordState = room.getLandlordState();
        final int playerId = action.getPlayerId();
        int currentPlayerId = room.getCurrentPlayerId();
        ActionType actionType = action.getType();
        LandlordCheckResult landlordCheckResult = LandlordRuleChecker.validateCanCallLandlord(room);

        // 地主阶段的业务非法状态统一返回 rejected，而不是依赖异常让服务端兜底。
        if (landlordCheckResult == LandlordCheckResult.WRONG_PHASE) {
            return GameResult.rejected("当前阶段不能抢地主", playerId);
        }

        if (landlordCheckResult == LandlordCheckResult.LANDLORD_ALREADY_DECIDED) {
            return GameResult.rejected("地主已确定", playerId);
        }

        if (actionType == null) {
            return GameResult.rejected("你在干赣神魔", playerId);
        }

        if (currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就叫", playerId);
        }

        // 叫地主阶段已经不叫的玩家，在抢地主阶段强制PASS
        List<Integer> callPassPlayerIds = landlordState.getCallPassPlayerIds();
        if (callPassPlayerIds.contains(currentPlayerId)) {
            actionType = ActionType.PASS;
        }

        // 抢地主
        if (ActionType.CALL == actionType) {
            landlordState.setLandlordCandidateId(currentPlayerId);
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            return resolveRobResult(room, landlordState, currentPlayerId, "抢地主");
        }

        // 不抢地主
        if (ActionType.PASS == actionType) {
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            return resolveRobResult(room, landlordState, currentPlayerId, "不抢地主");
        }

        return GameResult.rejected("当前操作无效", playerId);
    }

    /**
     * 解析抢地主的结果。
     * <p>
     * 这里把“是否立即确认地主”收敛成一个出口，避免 CALL/PASS 两个分支各自维护结束条件。
     *
     * @param room 游戏房间对象
     * @param landlordState 地主状态信息
     * @param currentPlayerId 当前玩家ID
     * @param acceptedMessage 接受消息时返回的消息内容
     * @return 根据当前游戏状态返回的处理结果，包括地主确认或动作被接受的信息
     */
    private GameResult resolveRobResult(GameRoom room, LandlordState landlordState, int currentPlayerId, String acceptedMessage) {
        Integer firstCallerId = landlordState.getFirstCallerId();
        Integer landlordCandidateId = landlordState.getLandlordCandidateId();
        Integer nextPlayerId = room.getCurrentPlayerId();

        if (Integer.valueOf(currentPlayerId)
                .equals(firstCallerId)) {
            gameFlow.confirmLandlord(room, landlordCandidateId);
            return GameResult.landlordDecided("地主确认：玩家 " + landlordCandidateId);
        }

        if (nextPlayerId.equals(firstCallerId) && firstCallerId.equals(landlordCandidateId)) {
            gameFlow.confirmLandlord(room, landlordCandidateId);
            return GameResult.landlordDecided("地主确认：玩家 " + landlordCandidateId);
        }

        return GameResult.accepted(acceptedMessage);
    }
}
