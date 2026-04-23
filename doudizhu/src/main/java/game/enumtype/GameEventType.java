package game.enumtype;

/**
 * 游戏事件类型枚举。
 * <p>
 * 定义游戏动作处理后产生的事件类型，用于服务端广播和流程控制。
 * </p>
 */
public enum GameEventType {
    /**
     * 无事件
     */
    NONE,
    /**
     * 动作被接受
     */
    ACTION_ACCEPTED,
    /**
     * 动作被拒绝
     */
    ACTION_REJECTED,
    /**
     * 需要重新发牌
     */
    REDEAL_REQUIRED,
    /**
     * 地主已确定
     */
    LANDLORD_DECIDED,
    HIGHEST_CARD_DECIDED
}
