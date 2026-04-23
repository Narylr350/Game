package rule.play;

/**
 * 出牌阶段的规则检查结果。
 */
public enum PlayCheckResult {
    /**
     * 当前操作符合出牌规则。
     */
    VALID,
    /**
     * 当前房间阶段不是出牌阶段。
     */
    WRONG_PHASE,
    /**
     * 本次提交的牌无法构成合法牌型。
     */
    INVALID_CARD_PATTERN,
    /**
     * 当前出牌和上一手牌型不一致。
     */
    CARD_TYPE_MISMATCH,
    /**
     * 当前出牌没有大过上一手。
     */
    NOT_STRONGER_THAN_LAST
}
