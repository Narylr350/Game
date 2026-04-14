package game.action;

import java.util.List;

public class GameAction {
    private final int playerId;                    // 玩家ID
    private final ActionType type;                 // 动作类型
    private final List<Integer> cards;             // 要打出的牌

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