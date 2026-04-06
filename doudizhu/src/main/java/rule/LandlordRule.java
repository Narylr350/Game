package rule;

import game.GamePhase;
import game.GameRoom;
import game.PlayerState;

//地主规则
public class LandlordRule {
    //是否能叫地主
    public static Boolean canCallLandlord(GameRoom room) {
        //房间不能为空
        if (room == null) {
            return false;
        }
        //当前阶段必须是地主阶段
        if (room.getPhase() != GamePhase.CALL_LANDLORD &&room.getPhase() != GamePhase.ROB_LANDLORD) {//@Rainbwo
            return false;
        }
        //玩家不能为空
        if (room.getPlayers() == null) {
            return false;
        }
        //不能有地主
        for (PlayerState player : room.getPlayers()) {
            if (player.isLandlord()) {
                return false;
            }
        }
        return true;
    }
}
