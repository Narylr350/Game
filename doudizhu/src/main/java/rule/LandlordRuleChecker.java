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
public class LandlordRuleChecker {
    /**
     * 验证是否可以叫地主。
     *
     * 该方法用于验证当前游戏房间的状态是否允许玩家叫地主。具体规则如下：
     * - 房间不能为空。
     * - 当前阶段必须是叫地主或抢地主阶段。
     * - 不能有已确定的地主。
     *
     * @param room 游戏房间对象，包含房间的所有状态信息
     * @throws IllegalStateException 如果验证失败，则抛出此异常
     */
    public static void validateCanCallLandlord(GameRoom room) {
        // 房间不能为空
        if (room == null) {
            throw new IllegalStateException("房间不能为空");
        }
        // 当前阶段必须是叫地主或抢地主阶段
        if (room.getCurrentPhase() != GamePhase.CALL_LANDLORD && room.getCurrentPhase() != GamePhase.ROB_LANDLORD) {
           throw new IllegalStateException("必须为CALL_LANDLORD或ROB_LANDLORD阶段");
        }
        // 不能有已确定的地主
        for (PlayerState player : room.getPlayers()) {
            if (player.isLandlord()) {
                throw new IllegalStateException("该阶段不能有地主");
            }
        }
    }
}
