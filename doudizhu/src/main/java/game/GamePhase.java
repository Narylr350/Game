package game;

/**
 * 游戏阶段枚举。
 * <p>
 * 定义一局斗地主游戏的各个阶段，用于控制游戏流程。
 * </p>
 */
public enum GamePhase {
    /** 等待阶段，游戏尚未开始 */
    WAITING,
    /** 发牌阶段，正在初始化手牌 */
    DEALING,
    /** 叫地主阶段，玩家依次决定是否叫地主 */
    CALL_LANDLORD,
    /** 抢地主阶段，玩家决定是否抢地主 */
    ROB_LANDLORD,
    /** 出牌阶段，玩家依次出牌对战 */
    PLAYING,
    /** 结算阶段，游戏结束并展示结果 */
    SETTLE
}
