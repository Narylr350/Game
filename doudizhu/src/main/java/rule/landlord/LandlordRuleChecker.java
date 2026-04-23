package rule.landlord;

import game.enumtype.GamePhase;
import game.model.GameRoom;
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
     * <p>
     * 该方法用于验证当前游戏房间的状态是否允许玩家叫地主。具体规则如下：
     * - 房间不能为空。
     * - 当前阶段必须是叫地主或抢地主阶段。
     * - 不能有已确定的地主。
     *
     * @param room 游戏房间对象，包含房间的所有状态信息
     * @return 返回地主阶段检查结果。业务非法状态通过返回值表达，只有room为空这类程序错误才抛异常
     * @throws IllegalStateException 当room为null时抛出
     */
    public static LandlordCheckResult validateCanCallLandlord(GameRoom room) {
        if (room == null) {
            throw new IllegalStateException("房间不能为空");
        }
        if (room.getCurrentPhase() != GamePhase.CALL_LANDLORD && room.getCurrentPhase() != GamePhase.ROB_LANDLORD) {
            return LandlordCheckResult.WRONG_PHASE;
        }
        for (PlayerState player : room.getPlayers()) {
            if (player.isLandlord()) {
                return LandlordCheckResult.LANDLORD_ALREADY_DECIDED;
            }
        }
        return LandlordCheckResult.VALID;
    }
}
