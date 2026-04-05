package game.handler;

import game.ActionResult;
import game.ActionType;
import game.GamePhase;
import game.GameRoom;
import rule.LandlordRule;

public class CallLanLordHandler {
    public ActionResult callLandLordHandler(GameRoom room, int playerId, ActionType actionType) {
        if (actionType == null) {
            return ActionResult.fail("你在干赣神魔");
        }
        if (room == null) {
            return ActionResult.fail("房间不能为空");
        }
        if (!LandlordRule.canCallLandlord(room)) {
            return ActionResult.fail("不能叫地主");
        }
        int currentTurnPlayerId = room.getCurrentTurnPlayerId();
        if (currentTurnPlayerId != playerId) {
            return ActionResult.fail("是你吗你就叫");
        }

        //抢地主+1分
        if (ActionType.CALL == actionType) {
//            //抢了地主改变状态 变为叫地主 @Rainbow
//            room.setPhase(GamePhase.ROB_LANDLORD);
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
            } else {
                room.setCurrentTurnPlayerId(playerId + 1);
            }
        }
        //不抢
        if (ActionType.PASS == actionType) {
            //记录不抢玩家数
            room.addPassCount();
            //设置轮数
            room.addActionCount();
            //轮到下一个玩家
            if (playerId == 3) {
                room.setCurrentTurnPlayerId(1);
            } else {
                room.setCurrentTurnPlayerId(playerId + 1);
            }
        }

        //先到2分的直接判断为地主
        if (room.getPlayerScores()
                .get(playerId) == 2 && room.getHighestScore() == 2) {
            //将该玩家设为地主
            room.findPlayerById(playerId)
                    .setLandlord(true);
            //将该玩家id设为地主id
            room.setLandlordId(playerId);
            //将底牌添加到地主手牌中
            room.findPlayerById(playerId)
                    .addCards(room.getHoleCards());
            return ActionResult.successLandlordConfirmed("你是地主了", playerId);
        }
        //如果三人都不抢
        if (room.getPassCount() == 3) {
            return ActionResult.failLandlordConfirmed("重开");
        }
        //在第一轮如果没有人到两分
        if (room.getActionCount() == 4 && room.getHighestScore() == 1) {
            room.setLandlordId(room.getLastHighestScorerId());
            return ActionResult.successLandlordConfirmed("你是地主了", playerId);
        }
        return ActionResult.success("", playerId, room.getCurrentTurnPlayerId());
    }
}
