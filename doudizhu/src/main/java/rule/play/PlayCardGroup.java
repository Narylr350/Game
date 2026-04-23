package rule.play;

import util.CardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 分析给定的牌集合，确定其组合成的牌型。
     *
     * @param cards 一个整数集合，表示牌的ID。每张牌通过一个唯一的整数标识。
     * @return 返回一个PlayCardGroup对象，该对象包含分析结果，如牌型、主牌点数等信息。
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
            return buildGroup(CardType.SINGLE, ranks, countMap, size);
        }
        //对子
        if (size == 2 && countMap.size() == 1) {
            return buildGroup(CardType.PAIR, ranks, countMap, size);
        }
        //三张
        if (size == 3 && countMap.size() == 1) {
            return buildGroup(CardType.TRIPLE, ranks, countMap, size);
        }
        //三带一
        if (isThreeWithOne(size, countMap)) {
            return buildGroup(CardType.THREE_WITH_ONE, ranks, countMap, size);
        }
        //三带二
        if (isThreeWithPair(size, countMap)) {
            return buildGroup(CardType.THREE_WITH_PAIR, ranks, countMap, size);
        }
        //炸弹
        if (size == 4 && countMap.size() == 1) {
            return buildGroup(CardType.BOMB, ranks, countMap, size);
        }
        //火箭
        if (size == 2 && ranks.contains(CardUtil.SMALL_JOKER_RANK) && ranks.contains(CardUtil.BIG_JOKER_RANK)) {
            return buildGroup(CardType.ROCKET, ranks, countMap, size);
        }
        //飞机
        if (isAirplane(size, ranks, countMap)) {
            return buildGroup(CardType.AIRPLANE, ranks, countMap, size);
        }
        //飞机带两单
        if (isAirplaneWithSingleWings(size, ranks, countMap)) {
            return buildGroup(CardType.AIRPLANE_WITH_SINGLE_WINGS, ranks, countMap, size);
        }
        //飞机带两对
        if (isAirplaneWithPairWings(size, ranks, countMap)) {
            return buildGroup(CardType.AIRPLANE_WITH_PAIR_WINGS, ranks, countMap, size);
        }
        //四带两单
        if (isFourWithTwoSingle(size, countMap)) {
            return buildGroup(CardType.FOUR_WITH_TWO_SINGLE, ranks, countMap, size);
        }
        //四带两对
        if (isFourWithTwoPair(size, countMap)) {
            return buildGroup(CardType.FOUR_WITH_TWO_PAIR, ranks, countMap, size);
        }
        //顺子
        if (isStraight(size, ranks)) {
            return buildGroup(CardType.STRAIGHT, ranks, countMap, size);
        }
        //连对
        if (isConsecutivePairs(size, ranks, countMap)) {
            return buildGroup(CardType.CONSECUTIVE_PAIRS, ranks, countMap, size);
        }
        return new PlayCardGroup(CardType.INVALID, -1, 0);
    }

    /**
     * 构建一个牌组对象。
     *
     * @param type 牌型，表示构建的牌组属于哪种牌型
     * @param ranks 牌的点数列表，每个元素代表一张牌的点数值
     * @param countMap 每个点数出现次数的映射，键为点数值，值为该点数出现的次数
     * @param size 牌组中牌的数量
     * @return 返回一个新的PlayCardGroup对象，包含牌型、主牌点数及牌组大小等信息
     */
    private static PlayCardGroup buildGroup(CardType type, List<Integer> ranks, Map<Integer, Integer> countMap, int size) {
        return new PlayCardGroup(type, extractMainRank(type, ranks, countMap, size), size);
    }

    /**
     * 从给定的牌型、点数列表、计数映射和牌组大小中提取主牌点数。
     *
     * @param type 牌型，表示当前牌组的类型
     * @param ranks 牌的点数列表，每个元素代表一张牌的点数值
     * @param countMap 每个点数出现次数的映射，键为点数值，值为该点数出现的次数
     * @param size 牌组中牌的数量
     * @return 返回主牌点数。如果无法确定主牌点数，则返回-1
     */
    private static int extractMainRank(CardType type, List<Integer> ranks, Map<Integer, Integer> countMap, int size) {
        return switch (type) {
            case SINGLE, PAIR, TRIPLE, BOMB, ROCKET, STRAIGHT, CONSECUTIVE_PAIRS -> maxRank(ranks);
            case THREE_WITH_ONE, THREE_WITH_PAIR -> rankByCount(countMap, 3);
            case FOUR_WITH_TWO_SINGLE, FOUR_WITH_TWO_PAIR -> rankByCount(countMap, 4);
            case AIRPLANE -> {
                List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, size / 3, false);
                yield bodyRanks.isEmpty() ? -1 : bodyRanks.getLast();
            }
            case AIRPLANE_WITH_SINGLE_WINGS -> {
                List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, size / 4, true);
                yield bodyRanks.isEmpty() ? -1 : bodyRanks.getLast();
            }
            case AIRPLANE_WITH_PAIR_WINGS -> {
                List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, size / 5, true);
                yield bodyRanks.isEmpty() ? -1 : bodyRanks.getLast();
            }
            default -> -1;
        };
    }

    /**
     * 根据给定的计数映射和目标计数，找到具有最大键值且其值等于目标计数的键。
     *
     * @param countMap 一个映射，键为点数值，值为该点数出现的次数
     * @param targetCount 目标计数，用于筛选映射中的条目
     * @return 返回满足条件的最大键值；如果没有找到符合条件的键，则返回-1
     */
    private static int rankByCount(Map<Integer, Integer> countMap, int targetCount) {
        return countMap.entrySet()
                .stream()
                .filter(s -> s.getValue() == targetCount)
                .map(Map.Entry::getKey)
                .max(Integer::compare)
                .orElse(-1);
    }

    /**
     * 从给定的牌计数映射中提取飞机本体点数组合。
     *
     * @param countMap 每个点数出现次数的映射，键为点数值，值为该点数出现的次数
     * @param bodyGroupCount 飞机本体组的数量
     * @param allowBombSplit 是否允许炸弹拆分作为三张一组的情况
     * @return 返回一个整数列表，表示找到的飞机本体点数序列；如果未找到符合条件的组合，则返回空列表
     */
    private static List<Integer> extractAirplaneBodyRanks(Map<Integer, Integer> countMap, int bodyGroupCount, boolean allowBombSplit) {
        List<Integer> tripleRanks = countMap.entrySet()
                .stream()
                .filter(s -> allowBombSplit ? s.getValue() >= 3 : s.getValue() == 3)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        if (tripleRanks.size() < bodyGroupCount) {
            return List.of();
        }

        for (int start = 0; start <= tripleRanks.size() - bodyGroupCount; start++) {
            List<Integer> bodyRanks = tripleRanks.subList(start, start + bodyGroupCount);

            if (!isConsecutive(bodyRanks)) {
                continue;
            }
            if (bodyRanks.contains(CardUtil.TWO_RANK)
                    || bodyRanks.contains(CardUtil.SMALL_JOKER_RANK)
                    || bodyRanks.contains(CardUtil.BIG_JOKER_RANK)) {
                continue;
            }
            return bodyRanks;
        }
        return List.of();
    }

    /**
     * 检查给定的排序后的点数列表是否构成连续序列。
     *
     * @param sortedRanks 一个整数列表，表示已按升序排列的牌点数值
     * @return 如果点数列表中的所有元素都与其前一个元素相差1，则返回true；否则返回false
     */
    private static boolean isConsecutive(List<Integer> sortedRanks) {
        for (int i = 1; i < sortedRanks.size(); i++) {
            if (!sortedRanks.get(i)
                    .equals(sortedRanks.get(i - 1) + 1)) {
                return false;
            }
        }
        return true;
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

    /**
     * 判断给定的牌是否构成四带两单（即四张相同点数的牌加上两张不同点数的单牌）的牌型。
     *
     * @param size     牌的数量
     * @param countMap 每个点数出现次数的映射
     * @return 如果给定的牌构成四带两单则返回true，否则返回false
     */
    private static boolean isFourWithTwoSingle(int size, Map<Integer, Integer> countMap) {
        if (size != 6) {
            return false;
        }
        return countMap.containsValue(4);
    }

    /**
     * 判断给定的牌是否构成四带两对的牌型。
     *
     * @param size     牌的数量
     * @param countMap 每个点数出现次数的映射，键为点数值，值为该点数出现的次数
     * @return 如果给定的牌构成四带两对则返回true，否则返回false
     */
    private static boolean isFourWithTwoPair(int size, Map<Integer, Integer> countMap) {
        if (size != 8) {
            return false;
        }
        return countMap.containsValue(4) && countMap.containsValue(2) && !countMap.containsValue(1);
    }

    /**
     * 判断给定的牌是否构成带单张翅膀的飞机（即三张相同点数的牌组成一组，且至少有两组这样的牌，这些组之间点数连续，每组还带有一张同点数的牌作为翅膀）。
     *
     * @param size     牌的数量
     * @param ranks    牌的点数列表，每个元素代表一张牌的点数值
     * @param countMap 每个点数出现次数的映射
     * @return 如果给定的牌构成带单张翅膀的飞机则返回true，否则返回false
     */
    //特殊,飞机本体不让带2,但是翅膀可以带2,所以单独处理本体和翅膀
    private static boolean isAirplaneWithSingleWings(int size, List<Integer> ranks, Map<Integer, Integer> countMap) {
        if (size < 8 || size % 4 != 0) {
            return false;
        }

        int bodyGroupCount = size / 4;
        List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, bodyGroupCount, true);
        if (bodyRanks.isEmpty()) {
            return false;
        }

        Map<Integer, Integer> remainingCountMap = new HashMap<>(countMap);
        for (Integer bodyRank : bodyRanks) {
            remainingCountMap.put(bodyRank, remainingCountMap.get(bodyRank) - 3);
            if (remainingCountMap.get(bodyRank) == 0) {
                remainingCountMap.remove(bodyRank);
            }
        }

        int remainingCards = 0;
        for (Integer count : remainingCountMap.values()) {
            remainingCards += count;
            if (count != 1) {
                return false;
            }
        }

        return remainingCards == bodyGroupCount;
    }

    /**
     * 判断给定的牌是否构成带对子翅膀的飞机（即三张相同点数的牌组成一组，且至少有两组这样的牌，这些组之间点数连续，每组还带有一对同点数的牌作为翅膀）。
     *
     * @param size     牌的数量
     * @param ranks    牌的点数列表，每个元素代表一张牌的点数值
     * @param countMap 每个点数出现次数的映射
     * @return 如果给定的牌构成带对子翅膀的飞机则返回true，否则返回false
     */
    private static boolean isAirplaneWithPairWings(int size, List<Integer> ranks, Map<Integer, Integer> countMap) {
        if (size < 10 || size % 5 != 0) {
            return false;
        }

        int bodyGroupCount = size / 5;
        List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, bodyGroupCount, true);
        if (bodyRanks.isEmpty()) {
            return false;
        }

        Map<Integer, Integer> remainingCountMap = new HashMap<>(countMap);
        for (Integer bodyRank : bodyRanks) {
            remainingCountMap.put(bodyRank, remainingCountMap.get(bodyRank) - 3);
            if (remainingCountMap.get(bodyRank) == 0) {
                remainingCountMap.remove(bodyRank);
            }
        }

        if (remainingCountMap.size() != bodyGroupCount) {
            return false;
        }

        for (Integer count : remainingCountMap.values()) {
            if (count != 2) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断给定的牌是否构成飞机（即三张相同点数的牌组成一组，且至少有两组这样的牌，这些组之间点数连续）。
     *
     * @param size     牌的数量
     * @param ranks    牌的点数列表，每个元素代表一张牌的点数值
     * @param countMap 每个点数出现次数的映射
     * @return 如果给定的牌构成飞机则返回true，否则返回false
     */
    private static boolean isAirplane(int size, List<Integer> ranks, Map<Integer, Integer> countMap) {
        if (size < 6 || size % 3 != 0) {
            return false;
        }

        List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, size / 3, false);
        return !bodyRanks.isEmpty();
    }

    private static boolean isThreeWithOne(int size, Map<Integer, Integer> countMap) {
        if (size != 4) {
            return false;
        }
        return countMap.containsValue(3) && countMap.containsValue(1);
    }

    private static boolean isThreeWithPair(int size, Map<Integer, Integer> countMap) {
        if (size != 5) {
            return false;
        }
        return countMap.containsValue(3) && countMap.containsValue(2);
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
        if (ranks.contains(CardUtil.TWO_RANK)
                || ranks.contains(CardUtil.SMALL_JOKER_RANK)
                || ranks.contains(CardUtil.BIG_JOKER_RANK)) {
            return false;
        }
        return isConsecutive(distinctSortRanks);
    }

    /**
     * 判断给定的牌列表是否构成连续对子。
     *
     * @param size     给定牌的数量
     * @param ranks    牌的点数列表，表示每张牌的点数
     * @param countMap 每个点数出现次数的映射
     * @return 如果给定的牌列表构成连续对子则返回true，否则返回false
     */
    private static boolean isConsecutivePairs(int size, List<Integer> ranks, Map<Integer, Integer> countMap) {
        if (size < 6) {
            return false;
        }
        if (ranks.contains(CardUtil.TWO_RANK)
                || ranks.contains(CardUtil.SMALL_JOKER_RANK)
                || ranks.contains(CardUtil.BIG_JOKER_RANK)) {
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

        return isConsecutive(distinctSortRanks);
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
