package rule;

import game.GameRoom;

import java.util.List;

// Stage-one placeholder: this only checks that a room, player, and card list exist.
public class PlayRuleChecker {
    public boolean canPlay(GameRoom room, int playerId, List<Integer> cards) {
        return room != null
                && room.findPlayerById(playerId) != null
                && cards != null
                && !cards.isEmpty();
    }
}
