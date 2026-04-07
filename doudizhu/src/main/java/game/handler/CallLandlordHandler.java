package game.handler;

import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import rule.LandlordRule;

public class CallLandlordHandler {

    public GameResult handle(GameRoom room, GameAction action) {
        int playerId = action.getPlayerId();
        ActionType actionType = action.getType();

        if (!LandlordRule.canCallLandlord(room)) {
            return GameResult.rejected("不能叫地主");
        }

        if (actionType == null) {
            return GameResult.rejected("你在干赣神魔", playerId);
        }

        Integer currentPlayerId = room.getCurrentPlayerId();
        if (currentPlayerId == null || currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就抢", playerId);
        }

        if (ActionType.CALL == actionType) {
            room.setLandlordCandidateId(currentPlayerId);
            room.setFirstCallerId(currentPlayerId);
            room.setCurrentPhase(GamePhase.ROB_LANDLORD);
            room.setCurrentPlayerId(nextPlayerId(currentPlayerId));
            return GameResult.accepted("叫地主成功", room.getCurrentPlayerId());
        }

        if (ActionType.PASS == actionType) {
            room.addCallPassPlayerId(currentPlayerId);
            room.setCurrentPlayerId(nextPlayerId(currentPlayerId));
            room.incrementCallPassCount();

            if (room.getCallPassCount() == 3) {
                return GameResult.redealRequired("");
            }

            return GameResult.accepted("不叫地主", room.getCurrentPlayerId());
        }

        return GameResult.rejected("当前操作无效", playerId);
    }

    private Integer nextPlayerId(Integer currentPlayerId) {
        if (currentPlayerId.equals(3)) {
            return 1;
        }
        return currentPlayerId + 1;
    }
}