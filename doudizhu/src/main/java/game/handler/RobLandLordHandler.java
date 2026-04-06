package game.handler;

import game.ActionType;
import game.GameActionResult;
import game.GamePhase;
import game.GameRoom;

import java.util.List;

public class RobLandLordHandler {
    public GameActionResult robLandLordHandler(GameRoom room, Integer playerId, ActionType actionType) {
        if (actionType == null) {
            return GameActionResult.invalidAction("你在干赣神魔", playerId);
        }
        Integer currentTurnPlayerId = room.getCurrentTurnPlayerId();
        if (!currentTurnPlayerId.equals(playerId)) {
            return GameActionResult.invalidAction("是你吗你就叫", playerId);
        }
        List<Integer> passPlayerId = room.getPassPlayerId();
        if (passPlayerId.contains(currentTurnPlayerId)) {
            actionType = ActionType.PASS;
        }

        //抢地主
        if (ActionType.CALL == actionType) {
            //下一个
            room.setCurrentTurnPlayerId(nextPlayerId(currentTurnPlayerId));
            //将该玩家设置为地主候选人
            room.setLandLordCandidateId(currentTurnPlayerId);
        }
        //不抢
        if (ActionType.PASS == actionType) {
            //下一个
            room.setCurrentTurnPlayerId(nextPlayerId(currentTurnPlayerId));
        }
        //判断是不是地主
        if (currentTurnPlayerId.equals(room.getFirstCallerId())) {
            room.setPhase(GamePhase.PLAYING);
            room.setLandLordId(room.getLandLordCandidateId());
            return GameActionResult.landLordDecided("抢地主成功", room.getLandLordCandidateId());
        }

        return GameActionResult.actionAccepted("操作成功", currentTurnPlayerId, room.getCurrentTurnPlayerId());
    }

    private Integer nextPlayerId(Integer currentPlayerId) {
        if (currentPlayerId.equals(3)) {
            return 1;
        }
        return currentPlayerId + 1;
    }
}
