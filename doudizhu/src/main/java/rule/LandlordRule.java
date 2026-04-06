package rule;

import game.GamePhase;
import game.GameRoom;
import game.state.PlayerState;

/**
 * 地主规则类。
 * <p>
 * 包含地主相关的规则判断,主要提供是否可以叫地主的合法性检查。
 * </p>
 */
public class LandlordRule {
    /**
     * 判断当前房间状态是否允许叫地主。
     * <p>
     * 需要满足以下条件:
     * <ul>
     *   <li>房间对象不能为空</li>
     *   <li>当前阶段必须是叫地主(CALL_LANDLORD)或抢地主(ROB_LANDLORD)阶段</li>
     *   <li>玩家列表不能为空</li>
     *   <li>房间内还不能有地主(即不能已经确定地主)</li>
     * </ul>
     * </p>
     *
     * @param room 游戏房间对象
     * @return 如果满足所有条件则返回true,否则返回false
     */
    public static Boolean canCallLandlord(GameRoom room) {
        //房间不能为空
        if (room == null) {
            return false;
        }
        //当前阶段必须是地主阶段
        if (room.getCurrentPhase() != GamePhase.CALL_LANDLORD && room.getCurrentPhase() != GamePhase.ROB_LANDLORD) {//@Rainbwo
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
