package rule.landlord;

/**
 * 叫地主/抢地主阶段的规则检查结果。
 */
public enum LandlordCheckResult {
    /**
     * 当前操作符合地主阶段的基础规则。
     */
    VALID,
    /**
     * 当前房间阶段不是叫地主或抢地主阶段。
     */
    WRONG_PHASE,
    /**
     * 房间中已经存在已确认的地主。
     */
    LANDLORD_ALREADY_DECIDED
}
