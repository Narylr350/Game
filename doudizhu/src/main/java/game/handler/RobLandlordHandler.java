package game.handler;

import game.GameActionResult;
import game.GamePhase;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;

import java.util.List;

public class RobLandlordHandler {

    public GameActionResult handle(GameRoom room, GameAction action) {
        int playerId = action.getPlayerId();
        ActionType actionType = action.getType();

        if (actionType == null) {
            return GameActionResult.invalidAction("你在干赣神魔", playerId);
        }

        Integer currentPlayerId = room.getCurrentPlayerId();
        if (currentPlayerId == null || !currentPlayerId.equals(playerId)) {
            return GameActionResult.invalidAction("是你吗你就叫", playerId);
        }

        List<Integer> callPassPlayerIds = room.getCallPassPlayerIds();
        if (callPassPlayerIds.contains(currentPlayerId)) {
            actionType = ActionType.PASS;
        }

        if (ActionType.CALL == actionType) {
            room.setCurrentPlayerId(nextPlayerId(currentPlayerId));
            room.setLandlordCandidateId(currentPlayerId);
        } else if (ActionType.PASS == actionType) {
            room.setCurrentPlayerId(nextPlayerId(currentPlayerId));
        } else {
            return GameActionResult.invalidAction("当前操作无效", playerId);
        }

        if (currentPlayerId.equals(room.getFirstCallerId())) {
            room.setCurrentPhase(GamePhase.PLAYING);
            room.setLandlordPlayerId(room.getLandlordCandidateId());

            PlayerState player = room.getPlayerById(room.getLandlordPlayerId());
            if (player != null) {
                player.addCards(room.getHoleCards());
            }

            return GameActionResult.landlordDecided("抢地主成功", room.getLandlordCandidateId());
        }

        return GameActionResult.actionAccepted("操作成功", room.getCurrentPlayerId());
    }

    private Integer nextPlayerId(Integer currentPlayerId) {
        if (currentPlayerId.equals(3)) {
            return 1;
        }
        return currentPlayerId + 1;
    }
}