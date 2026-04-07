package game.handler;

import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;

import java.util.List;

/**
 * 抢地主阶段处理器。
 * <p>
 * 负责处理玩家在抢地主阶段的操作。之前叫过地主的玩家在本轮会被强制PASS。
 * </p>
 */
public class RobLandlordHandler {

    /**
     * 处理抢地主阶段的玩家动作。
     *
     * @param room   游戏房间对象
     * @param action 玩家动作
     * @return 处理结果
     */
    public GameResult handle(GameRoom room, GameAction action) {
        Integer currentPlayerId = room.getCurrentPlayerId();
        int playerId = action.getPlayerId();
        ActionType actionType = action.getType();

        if (actionType == null) {
            return GameResult.rejected("你在干赣神魔", currentPlayerId);
        }

        if (currentPlayerId == null || !currentPlayerId.equals(playerId)) {
            return GameResult.rejected("是你吗你就叫", playerId);
        }

        // 之前叫地主阶段选择不叫的玩家，在本轮强制PASS
        List<Integer> callPassPlayerIds = room.getCallPassPlayerIds();
        if (callPassPlayerIds.contains(currentPlayerId)) {
            actionType = ActionType.PASS;
        }

        // 抢地主
        if (ActionType.CALL == actionType) {
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            room.setLandlordCandidateId(currentPlayerId);

            // 回到第一个叫地主的玩家，地主确认
            if (currentPlayerId.equals(room.getFirstCallerId())) {
                room.setCurrentPhase(GamePhase.PLAYING);
                room.setLandlordPlayerId(room.getLandlordCandidateId());

                PlayerState player = room.getPlayerById(room.getLandlordPlayerId());
                if (player != null) {
                    player.addCards(room.getHoleCards());
                }

                return GameResult.landlordDecided("地主确认：玩家 " + room.getLandlordPlayerId());
            }

            return GameResult.accepted("抢地主");

            // 不抢
        } else if (ActionType.PASS == actionType) {
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));

            // 回到第一个叫地主的玩家，地主确认
            if (currentPlayerId.equals(room.getFirstCallerId())) {
                room.setCurrentPhase(GamePhase.PLAYING);
                room.setLandlordPlayerId(room.getLandlordCandidateId());

                PlayerState player = room.getPlayerById(room.getLandlordPlayerId());
                if (player != null) {
                    player.addCards(room.getHoleCards());
                }

                return GameResult.landlordDecided("地主确认：玩家 " + room.getLandlordPlayerId());
            }

            return GameResult.accepted("不抢地主");
        }

        return GameResult.rejected("当前操作无效", currentPlayerId);
    }
}