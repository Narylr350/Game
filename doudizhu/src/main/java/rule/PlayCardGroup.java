package rule;

import util.CardUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PlayCardGroup {
    private final CardType type;
    private final int mainRank;
    private final int size;

    public PlayCardGroup(CardType type, int mainRank, int size) {
        this.type = type;
        this.mainRank = mainRank;
        this.size = size;
    }

    public CardType getType() {
        return type;
    }

    public int getMainRank() {
        return mainRank;
    }

    public int getSize() {
        return size;
    }

    public static PlayCardGroup of(String cards) {
        return parseCardType(cards);
    }

    private static PlayCardGroup parseCardType(String cards) {
        if (cards == null || cards.isEmpty()) {
            return new PlayCardGroup(CardType.INVALID, -1, 0);
        }
        cards = cards.trim()
                .replace(" ", "");
        List<String> tokenize = CardUtil.tokenize(cards);
        int count = 0;
        for (int i = 1; i < tokenize.size(); i++) {
            if (tokenize.get(i)
                    .equals(tokenize.get(i - 1))) {
                count++;
            }
        }
        //单张
        if (tokenize.size() == 1) {
            return new PlayCardGroup(CardType.SINGLE, CardUtil.getRank(tokenize.getFirst()), 1);
        }
        //对子
        if (tokenize.size() == 2) {
            return new PlayCardGroup(CardType.PAIR, CardUtil.getRank(tokenize.getFirst()), 2);
        }
        return null;
    }
}
