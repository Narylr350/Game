package game.handler;

import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;
import game.state.PlayingState;
import rule.PlayingRuleChecker;

import java.util.List;

public class PlayingHandler {
    public GameResult handle(GameRoom room, GameAction action) {
        PlayingState playingState = room.getPlayingState();
        final int playerId = action.getPlayerId();
        int currentPlayerId = room.getCurrentPlayerId();
        ActionType actionType = action.getType();
        List<Integer> cards = action.getCards();
        PlayerState currentPlayer = room.getPlayerById(currentPlayerId);

        PlayingRuleChecker.validateCanPlay(room, playerId, cards);

        if (currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就出", playerId);
        }
        // 出牌
        if (ActionType.PLAY_CARD == actionType) {
            boolean removed = currentPlayer.removeCards(cards);
            if (!removed) {
                return GameResult.rejected(playerId + "使用了无中生有", playerId);
            }
            playingState.setLastPlayedCards(cards);
            playingState.setHighestCardPlayerId(playerId);
            currentPlayer.removeCards(cards);
            return GameResult.accepted("出牌");
        }
        // 不出
        if (ActionType.PASS_CARD == actionType) {
            playingState.addPassCount();
            room.setCurrentPlayerId(room.getNextPlayerId(currentPlayerId));
            return GameResult.accepted("不出");
        }
        return GameResult.rejected("当前操作无效", playerId);
    }
}
