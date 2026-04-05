package game.handler;

import game.ActionResult;
import game.ActionType;
import game.GamePhase;
import game.GameRoom;
import rule.LandlordRule;

public class CallLanLordHandler {
    public ActionResult callLandLordHandler(GameRoom room, int playerId, ActionType actionType) {
        if (actionType == null) {
            return ActionResult.fail("动作不能为空");
        }
        if (room == null) {
            return ActionResult.fail("房间不能为空");
        }

        // 允许在 叫地主 / 抢地主 两个阶段进入
        if (room.getPhase() != GamePhase.CALL_LANDLORD
                && room.getPhase() != GamePhase.ROB_LANDLORD) {
            return ActionResult.fail("当前阶段不能叫/抢地主");
        }

        int currentTurnPlayerId = room.getCurrentTurnPlayerId();
        if (currentTurnPlayerId != playerId) {
            return ActionResult.fail("还没轮到你");
        }

        // 叫 / 抢
        if (ActionType.CALL == actionType) {
            // 只有第一轮“叫地主”后，才切到“抢地主”阶段
            if (room.getPhase() == GamePhase.CALL_LANDLORD) {
                room.setPhase(GamePhase.ROB_LANDLORD);
            }

            // 玩家分数 +1
            room.addPlayerScores(playerId);

            // 更新最高分
            if (room.getPlayerScores().get(playerId) >= room.getHighestScore()) {
                room.setHighestScore(room.getPlayerScores().get(playerId));
            }

            // 记录当前最高分持有者
            room.setLastHighestScorerId(playerId);

            // 记录操作次数
            room.addActionCount();

            // 切到下一个玩家
            nextPlayer(room, playerId);
        }
        // 不叫 / 不抢
        else if (ActionType.PASS == actionType) {
            room.addPassCount();
            room.addActionCount();

            // 第一轮“不叫”后，也要进入抢地主阶段
            if (room.getPhase() == GamePhase.CALL_LANDLORD) {
                room.setPhase(GamePhase.ROB_LANDLORD);
            }

            nextPlayer(room, playerId);
        } else {
            return ActionResult.fail("不支持的动作类型");
        }

        // 先到 2 分，直接成为地主
        if (room.getPlayerScores().get(playerId) == 2 && room.getHighestScore() == 2) {
            room.findPlayerById(playerId).setLandlord(true);
            room.setLandlordId(playerId);
            room.findPlayerById(playerId).addCards(room.getHoleCards());
            return ActionResult.successLandlordConfirmed("你是地主了", playerId);
        }

        // 三人都放弃，流局
        if (room.getPassCount() == 3) {
            return ActionResult.failLandlordConfirmed("重开");
        }

        // 第一轮+抢地主阶段结束后，最高分为 1，则最高分者当地主
        if (room.getActionCount() == 4 && room.getHighestScore() == 1) {
            int landlordId = room.getLastHighestScorerId();
            room.findPlayerById(landlordId).setLandlord(true);
            room.setLandlordId(landlordId);
            room.findPlayerById(landlordId).addCards(room.getHoleCards());
            return ActionResult.successLandlordConfirmed("你是地主了", landlordId);
        }

        return ActionResult.success("", playerId, room.getCurrentTurnPlayerId());
    }

    private void nextPlayer(GameRoom room, int playerId) {
        if (playerId == 3) {
            room.setCurrentTurnPlayerId(1);
        } else {
            room.setCurrentTurnPlayerId(playerId + 1);
        }
    }
}