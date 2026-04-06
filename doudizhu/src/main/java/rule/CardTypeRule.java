package rule;

import java.util.List;

/**
 * 牌型规则类。
 * <p>
 * 负责识别和判断出牌的牌型(如单张、对子、顺子、炸弹等)。
 * </p>
 * <p>
 * <b>注意:</b> 第1阶段只是先占住规则入口,真正牌型识别放到后续阶段实现。
 * 当前所有牌型都会被识别为INVALID(非法牌型)。
 * </p>
 */
public class CardTypeRule {
    /**
     * 识别给定牌组的牌型。
     *
     * @param cards 牌ID列表,表示一组要打出的牌
     * @return 识别出的牌型枚举,当前阶段始终返回CardType.INVALID
     */
    public CardType identify(List<Integer> cards) {
        return CardType.INVALID;
    }
}
