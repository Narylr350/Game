package game.action;

import java.util.List;

public class GameAction {
    private final int playerId;
    private final ActionType type;
    private final List<Integer> cards;

    public GameAction(int playerId, ActionType type, List<Integer> cards) {
        this.playerId = playerId;
        this.type = type;
        this.cards = cards;
    }

    public int getPlayerId() {
        return playerId;
    }

    public ActionType getType() {
        return type;
    }

    public List<Integer> getCards() {
        return cards;
    }
}