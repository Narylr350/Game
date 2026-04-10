package game.handler;

import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayState;
import game.state.PlayerState;
import rule.PlayRuleChecker;

import java.util.List;

public class PlayingHandler {
    public GameResult handle(GameRoom room, GameAction action) {
        PlayState playState = room.getPlayState();
        final int playerId = action.getPlayerId();
        int currentPlayerId = room.getCurrentPlayerId();
        ActionType actionType = action.getType();
        List<Integer> cards = action.getCards();

        PlayRuleChecker.validateCanPlay(room, playerId, cards);

        if (currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就出", playerId);
        }
        // 出牌
        if (ActionType.PLAY_CARD == actionType) {
            playState.setLastPlayedCards(cards);
            PlayerState currentPlayer = room.getPlayerById(currentPlayerId);
            currentPlayer.removeCards(cards);
            return GameResult.accepted("出牌");
        }
        // 不出
        if (ActionType.PASS == actionType) {

            return GameResult.accepted("不出");
        }
        return GameResult.rejected("", playerId);
    }
}
