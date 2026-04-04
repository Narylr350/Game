package game.handler;

import game.ActionResult;
import game.ActionType;
import game.GameFlow;
import game.GameRoom;
import game.PlayerState;
import rule.LandlordRule;

import java.util.List;

public class CallLanLordHandler {
    public ActionResult callLandLordHandler(GameRoom room, ActionType actionType) {
        //拿服务器返回的当前玩家ID和输入内容，服务器将字符串转换成ActionType里面的类型
        //这里使用固定数据占位
        Integer playerId = 1;
        if (LandlordRule.canCallLandlord(room) && ActionType.fromString("抢") == null) {
            //状态异常
            if (playerId == null) {
                return ActionResult.fail("谁干的好事");
            }
            return ActionResult.fail("别乱输了", playerId);
        } else {
            ActionResult result = isLandLord(room, actionType);
            //没人抢地主
            //有人抢地主成功
            if (result != null && result.isReDeal()) {
                return ActionResult.succelandlordConfirmedss(result.getMessage(), result.isReDeal());
            }
        }
        return null;
    }

    private ActionResult isLandLord(GameRoom room, ActionType actionType) {
        List<PlayerState> players = room.getPlayers();
        int actionCount = 0;
        int notCalledCount = 0;
        //抢地主+1分
        for (PlayerState player : players) {
            //当前轮到该玩家
            int playerId = player.getPlayerId();
            if (room.getCurrentTurnPlayerId() == playerId) {
                if (ActionType.CALL == actionType) {
                    //玩家分数 +1
                    player.addScore();
                    //更新最高分
                    room.setHighestScore(player.getScore());
                    //记录该玩家为当前最高分持有者
                    room.setLastHighestScorerId(playerId);
                    //设置轮数
                    room.setActionCount(++actionCount);
                    //轮到下一个玩家
                    if (playerId == 3) {
                        room.setCurrentTurnPlayerId(1);
                        return ActionResult.success("抢地主", playerId, 1);
                    } else {
                        room.setCurrentTurnPlayerId(playerId + 1);
                        return ActionResult.success("抢地主", playerId, playerId + 1);
                    }
                } else if (ActionType.NOT_CALLED == actionType) {
                    //不抢
                    //记录不抢玩家数
                    room.setNotCalledCount(++notCalledCount);
                    //设置轮数
                    room.setActionCount(++actionCount);
                }
            }
        }
        //先到2分的直接判断为地主
        for (PlayerState player : players) {
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
        //如果三人都不抢
        if (notCalledCount == 3) {
            new GameFlow().reDeal();
            return ActionResult.notLandLord("重新发牌");
        }
        //在第一轮如果没有人到两分
        if (actionCount == 3 && room.getHighestScore() == 1) {
            room.setLandlordId(room.getLastHighestScorerId());
            return ActionResult.isLandLord("", room.getLandlordPlayerId());
        }
        return null;
    }
}
