package rule;

/**
 * 牌型枚举。
 * <p>
 * 定义斗地主中所有合法的牌型类型。
 * </p>
 */
public enum CardType {
    /** 非法牌型 */
    INVALID,
    /** 单张 */
    SINGLE,
    /** 对子 */
    PAIR,
    /** 三条 */
    TRIPLE,
    /** 顺子（5张及以上连续单牌） */
    STRAIGHT,
    /** 连对（3对及以上连续对子） */
    CONSECUTIVE_PAIRS,
    /** 飞机（2个及以上连续三条） */
    AIRPLANE,
    /** 炸弹（四张相同点数的牌） */
    BOMB,
    /** 火箭（大王+小王） */
    ROCKET
}
