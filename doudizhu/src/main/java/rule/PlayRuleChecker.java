package rule;

import game.GameRoom;

import java.util.List;

// 第 3 阶段会把真正的出牌合法性判断补进来，当前只做最小防御性校验。
public class PlayRuleChecker {
    public boolean canPlay(GameRoom room, int playerId, List<Integer> cards) {
        return room != null
                && room.findPlayerById(playerId) != null
                && cards != null
                && !cards.isEmpty();
    }
}
