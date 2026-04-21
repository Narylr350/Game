package rule;

import util.CardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    /**
     * 分析给定的牌集合，确定其牌型。
     *
     * @param cards 牌的集合，每个元素代表一张牌
     * @return 返回一个PlayCardGroup对象，表示分析后的牌组信息。如果输入为空或不构成任何有效牌型，则返回INVALID类型。
     */
    public static PlayCardGroup analyzeCards(Collection<Integer> cards) {
        if (cards == null || cards.isEmpty()) {
            return new PlayCardGroup(CardType.INVALID, -1, 0);
        }
        int size = cards.size();
        //ranks存储牌的大小 比如0索引牌在ranks中对应1大小
        List<Integer> ranks = new ArrayList<>();
        for (Integer card : cards) {
            ranks.add(getRank(card));
        }
        //map的值统计每张牌的出现的次数,键存储牌的大小,比如012索引,都对应1大小,map实际上存储为(1,3)
        Map<Integer, Integer> countMap = new HashMap<>();
        for (Integer rank : ranks) {
            countMap.put(rank, countMap.getOrDefault(rank, 0) + 1);
        }
        //单牌
        if (size == 1) {
            return new PlayCardGroup(CardType.SINGLE, maxRank(ranks), 1);
        }
        //对子
        if (size == 2 && countMap.size() == 1) {
            return new PlayCardGroup(CardType.PAIR, maxRank(ranks), 2);
        }
        //三张
        if (size == 3 && countMap.size() == 1) {
            return new PlayCardGroup(CardType.TRIPLE, maxRank(ranks), 3);
        }
        //炸弹
        if (size == 4 && countMap.size() == 1) {
            return new PlayCardGroup(CardType.BOMB, maxRank(ranks), 4);
        }
        //火箭
        if (size == 2 && ranks.contains(CardUtil.SMALL_JOKER_RANK) && ranks.contains(CardUtil.BIG_JOKER_RANK)) {
            return new PlayCardGroup(CardType.ROCKET, maxRank(ranks), 2);
        }
        //飞机
        if (isAirplane()) {
            return new PlayCardGroup(CardType.AIRPLANE, maxRank(ranks), 6);
        }
        //顺子
        if (isStraight(size, ranks)) {
            return new PlayCardGroup(CardType.STRAIGHT, maxRank(ranks), size);
        }
        //连对
        if (isConsecutivePairs(size, ranks, countMap)) {
            return new PlayCardGroup(CardType.CONSECUTIVE_PAIRS, maxRank(ranks), size);
        }
        return new PlayCardGroup(CardType.INVALID, -1, 0);
    }

    /**
     * 从给定的牌点数列表中找到最大的点数。
     *
     * @param ranks 牌的点数列表，每个元素代表一张牌的点数值
     * @return 返回列表中的最大点数。如果列表为空，则返回-1
     */
    private static int maxRank(List<Integer> ranks) {
        return ranks.stream()
                .max(Integer::compare)
                .orElse(-1);
    }
    private static boolean isAirplane(){
        return true;
    }
    /**
     * 判断给定的牌列表是否构成顺子。
     *
     * @param size  给定牌的数量
     * @param ranks 牌的点数列表，表示每张牌的点数
     * @return 如果给定的牌列表构成顺子则返回true，否则返回false
     */
    private static boolean isStraight(int size, List<Integer> ranks) {
        if (size < 5) {
            return false;
        }
        List<Integer> distinctSortRanks = ranks.stream()
                .distinct()
                .sorted()
                .toList();
        if (distinctSortRanks.size() != ranks.size()) {
            return false;
        }
        if (ranks.contains(CardUtil.TWO_RANK) || ranks.contains(CardUtil.SMALL_JOKER_RANK) || ranks.contains(CardUtil.BIG_JOKER_RANK)) {
            return false;
        }
        for (int i = 1; i <= distinctSortRanks.size() - 1; i++) {
            if (!distinctSortRanks.get(i)
                    .equals(distinctSortRanks.get(i - 1) + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断给定的牌列表是否构成连续对子。
     *
     * @param size  给定牌的数量
     * @param ranks 牌的点数列表，表示每张牌的点数
     * @return 如果给定的牌列表构成连续对子则返回true，否则返回false
     */
    private static boolean isConsecutivePairs(int size, List<Integer> ranks, Map<Integer, Integer> countMap) {
        if (size < 6) {
            return false;
        }
        if (ranks.contains(CardUtil.TWO_RANK) || ranks.contains(CardUtil.SMALL_JOKER_RANK) || ranks.contains(CardUtil.BIG_JOKER_RANK)) {
            return false;
        }
        List<Integer> distinctSortRanks = ranks.stream()
                .distinct()
                .sorted()
                .toList();
        if (distinctSortRanks.size() == ranks.size()) {
            return false;
        }
        for (Integer distinctSortRank : distinctSortRanks) {
            if (!countMap.get(distinctSortRank)
                    .equals(2)) {
                return false;
            }
        }

        for (int i = 1; i <= distinctSortRanks.size() - 1; i++) {
            if (!distinctSortRanks.get(i)
                    .equals(distinctSortRanks.get(i - 1) + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 根据给定的牌ID获取其对应的点数。
     *
     * @param cardId 牌的唯一标识符，范围在0到53之间（包括0和53），其中52代表小王，53代表大王
     * @return 返回牌的点数，对于普通牌为1-13，小王返回14，大王返回15
     * @throws IllegalArgumentException 如果提供的牌ID不在合法范围内
     */
    private static int getRank(int cardId) {
        if (cardId < 0 || cardId > 53) {
            throw new IllegalArgumentException("非法牌索引: " + cardId);
        }
        if (cardId <= 51) {
            return cardId / 4 + 1;
        }
        return cardId == 52 ? 14 : 15;
    }
}
