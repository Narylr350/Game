package game.handler;

import game.ActionResult;
import game.GameRoom;
import game.PlayerState;
import rule.LandlordRule;

public class CallLanLordHandler {
    public ActionResult callLandLordHandler(GameRoom room) {
        if (!LandlordRule.canCallLandlord(room)) {
            ActionResult result = isLandLord(room);
            //没人抢地主
            if (result.isReDeal()) {
                return ActionResult.success(result.getMessage(), result.isReDeal());
            } else {
                //有人抢地主成功
                return ActionResult.success("", false);
            }
        } else {
            //状态异常

            return ActionResult.fail("谁干的好事");
        }
    }

    private ActionResult isLandLord(GameRoom room) {
        //先到2分的直接判断为地主
        for (PlayerState player : room.getPlayers()) {
            if (player.getScore() == 2) {
                //将该玩家设为地主
                player.setLandlord(true);
                //将该玩家id设为地主id
                int playerId = player.getPlayerId();
                room.setLandlordId(playerId);
                //将底牌添加到地主手牌中
                player.addCards(room.getHoleCards());
                return ActionResult.isLandLord("", player.getPlayerId());
            }
        }
        //不抢地主的计数器
        int count = 0;
        for (PlayerState player : room.getPlayers()) {
            if (player.getScore() == 0) {
                count++;
            }
        }
        //如果三人都不抢
        if (count == 3) {
            return ActionResult.notLandLord("");
        }
        return null;
    }
}
