package game.handler;

import game.GameActionResult;
import game.GamePhase;
import game.GameRoom;
import game.action.ActionType;
import rule.LandlordRule;

public class CallLandLordHandler {

    public GameActionResult handle(GameRoom room, int playerId, ActionType actionType) {
        if (!LandlordRule.canCallLandlord(room)) {
            return GameActionResult.invalidAction("不能叫地主");
        }

        if (actionType == null) {
            return GameActionResult.invalidAction("你在干赣神魔", playerId);
        }

        Integer currentPlayerId = room.getCurrentPlayerId();
        if (currentPlayerId == null || currentPlayerId != playerId) {
            return GameActionResult.invalidAction("是你吗你就抢", playerId);
        }

        if (ActionType.CALL == actionType) {
            room.setLandlordCandidateId(currentPlayerId);
            room.setFirstCallerId(currentPlayerId);
            room.setCurrentPhase(GamePhase.ROB_LANDLORD);
            room.setCurrentPlayerId(nextPlayerId(currentPlayerId));
            return GameActionResult.actionAccepted("叫地主成功", room.getCurrentPlayerId());
        }

        if (ActionType.PASS == actionType) {
            room.addCallPassPlayerId(currentPlayerId);
            room.setCurrentPlayerId(nextPlayerId(currentPlayerId));
            room.incrementCallPassCount();

            if (room.getCallPassCount() == 3) {
                return GameActionResult.redeal("重开");
            }

            return GameActionResult.actionAccepted("不叫地主", room.getCurrentPlayerId());
        }

        return GameActionResult.invalidAction("当前操作无效", playerId);
    }

    private Integer nextPlayerId(Integer currentPlayerId) {
        if (currentPlayerId.equals(3)) {
            return 1;
        }
        return currentPlayerId + 1;
    }
}