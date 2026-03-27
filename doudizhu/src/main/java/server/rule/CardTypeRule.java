package server.rule;

import java.util.List;

public class CardTypeRule {
    public CardType identify(List<Integer> cards) {
        return CardType.INVALID;
    }
}
