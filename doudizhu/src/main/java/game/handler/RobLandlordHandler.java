package game.handler;

import game.GameFlow;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
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
     * @param room 游戏房间对象
     * @param action 玩家动作
     * @return 处理结果
     */
    public GameResult handle(GameRoom room, GameAction action) {
        Integer currentPlayerId = room.getCurrentPlayerId();
        int playerId = action.getPlayerId();
        ActionType actionType = action.getType();

        if (actionType == null) {
            return GameResult.rejected("你在干赣神魔", playerId);
        }

        if (currentPlayerId == null || !currentPlayerId.equals(playerId)) {
            return GameResult.rejected("是你吗你就叫", playerId);
        }

        // 叫地主阶段已经不叫的玩家，在抢地主阶段强制PASS
        List<Integer> callPassPlayerIds = room.getCallPassPlayerIds();
        if (callPassPlayerIds.contains(currentPlayerId)) {
            actionType = ActionType.PASS;
        }

        // 抢地主
        if (ActionType.CALL == actionType) {
            room.setLandlordCandidateId(currentPlayerId);
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));

            // 如果下一位已经回到 firstCaller，说明这一轮表态结束，直接确认地主
            if (room.getCurrentPlayerId().equals(room.getFirstCallerId())) {
                Integer landlordId = room.getLandlordCandidateId();
                gameFlow.confirmLandlord(room, landlordId);
                return GameResult.landlordDecided("地主确认：玩家 " + landlordId);
            }

            return GameResult.accepted("抢地主");
        }

        // 不抢地主
        if (ActionType.PASS == actionType) {
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));

            // 如果下一位已经回到 firstCaller，说明这一轮表态结束，直接确认地主
            if (room.getCurrentPlayerId().equals(room.getFirstCallerId())) {
                Integer landlordId = room.getLandlordCandidateId();
                gameFlow.confirmLandlord(room, landlordId);
                return GameResult.landlordDecided("地主确认：玩家 " + landlordId);
            }

            return GameResult.accepted("不抢地主");
        }

        return GameResult.rejected("当前操作无效", playerId);
    }
}