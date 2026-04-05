package game.handler;

import game.ActionResult;
import game.ActionType;
import game.GameFlow;
import game.GameRoom;
import rule.LandlordRule;

public class CallLanLordHandler {
    public ActionResult callLandLordHandler(GameRoom room, Integer playerId, ActionType actionType) {
        if (actionType == null) {
            return ActionResult.fail("当前输入不合法");
        }
        if (room == null) {
            return ActionResult.fail("房间不能为空");
        }
        if (!LandlordRule.canCallLandlord(room)) {
            return ActionResult.fail("不能叫地主");
        }
        Integer currentTurnPlayerId = room.getCurrentTurnPlayerId();
        if (!currentTurnPlayerId
                .equals(playerId)) {
            return ActionResult.fail("是你吗你就叫");
        }

        //抢地主+1分
        if (ActionType.CALL == actionType) {
            //玩家分数 +1
            room.addPlayerScores(playerId);
            //更新最高分
            if (room.getPlayerScores()
                    .get(playerId) >= room.getHighestScore()) {
                room.setHighestScore(room.getPlayerScores()
                        .get(playerId));
            }
            //记录该玩家为当前最高分持有者
            room.setLastHighestScorerId(playerId);
            //设置轮数
            room.addActionCount();
            //轮到下一个玩家
            if (playerId == 3) {
                room.setCurrentTurnPlayerId(1);
            }
            room.setCurrentTurnPlayerId(playerId + 1);
        }
        if (ActionType.PASS == actionType) {
            //不抢
            //记录不抢玩家数
            room.addPassCount();
            //设置轮数
            room.addActionCount();
            if (playerId == 3) {
                room.setCurrentTurnPlayerId(1);

            }
            room.setCurrentTurnPlayerId(playerId + 1);

        }

        //先到2分的直接判断为地主
        if (room.getPlayerScores()
                .get(playerId) == 2 && room.getHighestScore() == 2) {
            //将该玩家设为地主
            room.findPlayerById(playerId).setLandlord(true);
            //将该玩家id设为地主id
            room.setLandlordId(playerId);
            //将底牌添加到地主手牌中
            room.findPlayerById(playerId).addCards(room.getHoleCards());
        }
        //在第一轮如果没有人到两分
        if (room.getActionCount() == 3 && room.getHighestScore() == 1) {
            room.setLandlordId(room.getLastHighestScorerId());
        }
        //如果三人都不抢
        if (room.getPassCount() == 3) {
            new GameFlow().reDeal(room);
        }
        return ActionResult.fail("111");
    }
}
