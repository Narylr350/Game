package rule;

import game.GameRoom;

// Stage-one placeholder: only basic room/player validation is wired for now.
public class LandlordRule {
    public void canCallLandlord(GameRoom room, int playerId) {
        if (room == null) {
            throw new IllegalArgumentException("对局不能为空");
        }
        if (room.findPlayerById(playerId) == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
    }
}
