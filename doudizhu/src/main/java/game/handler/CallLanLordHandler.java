package game.handler;

import game.ActionType;
import game.GameActionResult;
import game.GamePhase;
import game.GameRoom;
import rule.LandlordRule;

public class CallLanLordHandler {
    public GameActionResult callLandLordHandler(GameRoom room, int playerId, ActionType actionType) {
        if (!LandlordRule.canCallLandlord(room)) {
            return GameActionResult.invalidAction("不能叫地主");
        }
        if (actionType == null) {
            return GameActionResult.invalidAction("你在干赣神魔", playerId);
        }
        int currentTurnPlayerId = room.getCurrentTurnPlayerId();
        if (currentTurnPlayerId != playerId) {
            return GameActionResult.invalidAction("是你吗你就抢", playerId);
        }
        //叫地主
        if (ActionType.CALL == actionType) {
            //将改玩家设置为地主候选人
            room.setLandLordCandidateId(currentTurnPlayerId);
            //将该玩家设置为第一个叫地主的人
            room.setFirstCallerId(currentTurnPlayerId);
            //游戏阶段设置为抢地主阶段
            room.setPhase(GamePhase.ROB_LANDLORD);
            room.setCurrentTurnPlayerId(nextPlayerId(currentTurnPlayerId));
        }
        //不叫
        if (ActionType.PASS == actionType) {
            //不叫那就别叫了
            room.addPassPlayerId(currentTurnPlayerId);
            //下一个
            room.setCurrentTurnPlayerId(nextPlayerId(currentTurnPlayerId));
            //不抢计数器+1
            room.addPassCount();
        }
        //都不抢重开
        if (room.getPassCount() == 3) {
            return GameActionResult.redeal("重开");
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
