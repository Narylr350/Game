package game.action;

import game.GamePhase;

/**
 * 叫地主/抢地主动作类型枚举。
 * <p>
 * 定义玩家在叫地主和抢地主阶段可以选择的动作类型。
 * </p>
 */
public enum ActionType {

    /**
     * 叫地主/抢地主
     */
    CALL,
    /**
     * 不叫/不抢
     */
    PASS,
    /**
     * 出牌
     */
    PLAY_CARD,
    /**
     * 不出牌
     */
    PASS_CARD;

    /**
     * 解析玩家输入的动作字符串，转换为对应的ActionType枚举值。
     *
     * @param input 玩家输入的动作字符串
     * @param phase 当前消息类型，用于判断是叫地主还是抢地主阶段
     * @return 对应的ActionType枚举值，如果无法解析则返回null
     */
    public static ActionType parseAction(String input, GamePhase phase) {
        if (input == null || phase == null) {
            return null;
        }

        input = input.trim();

        switch (phase) {
            case CALL_LANDLORD:
                if ("1".equals(input) || "叫".equals(input) || "叫地主".equals(input)) {
                    return CALL;
                }
                if ("2".equals(input) || "不叫".equals(input)) {
                    return PASS;
                }
                break;

            case ROB_LANDLORD:
                if ("1".equals(input) || "抢".equals(input) || "抢地主".equals(input)) {
                    return CALL;
                }
                if ("2".equals(input) || "不抢".equals(input)) {
                    return PASS;
                }
                break;

            case PLAYING:
                return input.isBlank() ? PASS_CARD : PLAY_CARD;
        }

        return null;
    }
}