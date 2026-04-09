package game.handler;

import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import rule.PlayRuleChecker;

import java.util.List;

public class PlayingHandler {
    public GameResult handle(GameRoom room, GameAction action) {
        final int playerId = action.getPlayerId();
        int currentPlayerId = room.getCurrentPlayerId();
        ActionType actionType = action.getType();
        List<Integer> cards = action.getCards();

        PlayRuleChecker.validateCanPlay(room, playerId, cards);

        if (currentPlayerId != playerId) {
            return GameResult.rejected("是你吗你就出", playerId);
        }

        if (ActionType.PLAY_CARD == actionType) {
            
            return GameResult.accepted("");
        }
        if (ActionType.PASS == actionType) {

            return GameResult.accepted("");
        }
        return GameResult.rejected("", playerId);
    }
}
