package rule;

import java.util.List;

// 第 1 阶段只是先占住规则入口，真正牌型识别放到后续阶段实现。
public class CardTypeRule {
    public CardType identify(List<Integer> cards) {
        return CardType.INVALID;
    }
}
