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
     * @param room 游戏房间对象
     * @param action 玩家动作
     * @return 处理结果
     */
    public GameResult handle(GameRoom room, GameAction action) {
        int playerId = action.getPlayerId();
        ActionType actionType = action.getType();

        if (actionType == null) {
            return GameResult.rejected("你在干赣神魔", playerId);
        }

        Integer currentPlayerId = room.getCurrentPlayerId();
        if (currentPlayerId == null || !currentPlayerId.equals(playerId)) {
            return GameResult.rejected("是你吗你就叫", playerId);
        }

        // 之前叫过不叫的玩家，本轮强制不出
        List<Integer> callPassPlayerIds = room.getCallPassPlayerIds();
        if (callPassPlayerIds.contains(currentPlayerId)) {
            actionType = ActionType.PASS;
        }

        if (ActionType.CALL == actionType) {
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            room.setLandlordCandidateId(currentPlayerId);
        } else if (ActionType.PASS == actionType) {
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
        } else {
            return GameResult.rejected("当前操作无效", playerId);
        }
        // 回到第一个叫地主的玩家，本轮结束
        if (currentPlayerId.equals(room.getFirstCallerId())) {
            room.setCurrentPhase(GamePhase.PLAYING);
            room.setLandlordPlayerId(room.getLandlordCandidateId());
            room.resetCallPassCount();
            room.resetCallPasserId();
            room.resetFirstCallerId();
            room.resetLandlordCandidateId();
            PlayerState player = room.getPlayerById(room.getLandlordPlayerId());
            if (player != null) {
                player.addCards(room.getHoleCards());
            }

            return GameResult.landlordDecided("抢地主成功");
        }

        return GameResult.accepted("操作成功", room.getCurrentPlayerId());
    }
}