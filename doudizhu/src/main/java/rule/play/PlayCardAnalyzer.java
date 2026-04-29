package rule.play;

import util.CardUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 牌型分析器。
 * <p>
 * 负责把一组牌识别成斗地主牌型，并提取比较牌力时需要的主牌点数。
 * </p>
 */
public final class PlayCardAnalyzer {
    private PlayCardAnalyzer() {
    }

    /**
     * 分析给定的牌集合，确定其组合成的牌型。
     *
     * @param cards 一个整数集合，表示牌的ID。每张牌通过一个唯一的整数标识。
     * @return 牌型分析结果，包含牌型、主牌点数和牌数。
     */
    public static PlayCardGroup analyze(Collection<Integer> cards) {
        if (cards == null || cards.isEmpty()) {
            return new PlayCardGroup(CardType.INVALID, -1, 0);
        }
        int size = cards.size();

        // ranks 存储牌的点数，比如普通牌 0-3 都对应点数 1。
        List<Integer> ranks = new ArrayList<>();
        for (Integer card : cards) {
            ranks.add(getRank(card));
        }

        // countMap 统计每个点数出现几次，比如三张 3 会记录成 (1, 3)。
        Map<Integer, Integer> countMap = new HashMap<>();
        for (Integer rank : ranks) {
            countMap.put(rank, countMap.getOrDefault(rank, 0) + 1);
        }

        if (size == 1) {
            return buildGroup(CardType.SINGLE, ranks, countMap, size);
        }
        if (size == 2 && countMap.size() == 1) {
            return buildGroup(CardType.PAIR, ranks, countMap, size);
        }
        if (size == 3 && countMap.size() == 1) {
            return buildGroup(CardType.TRIPLE, ranks, countMap, size);
        }
        if (isThreeWithOne(size, countMap)) {
            return buildGroup(CardType.THREE_WITH_ONE, ranks, countMap, size);
        }
        if (isThreeWithPair(size, countMap)) {
            return buildGroup(CardType.THREE_WITH_PAIR, ranks, countMap, size);
        }
        if (size == 4 && countMap.size() == 1) {
            return buildGroup(CardType.BOMB, ranks, countMap, size);
        }
        if (size == 2 && ranks.contains(CardUtil.SMALL_JOKER_RANK) && ranks.contains(CardUtil.BIG_JOKER_RANK)) {
            return buildGroup(CardType.ROCKET, ranks, countMap, size);
        }
        if (isAirplane(size, countMap)) {
            return buildGroup(CardType.AIRPLANE, ranks, countMap, size);
        }
        if (isAirplaneWithSingleWings(size, countMap)) {
            return buildGroup(CardType.AIRPLANE_WITH_SINGLE_WINGS, ranks, countMap, size);
        }
        if (isAirplaneWithPairWings(size, countMap)) {
            return buildGroup(CardType.AIRPLANE_WITH_PAIR_WINGS, ranks, countMap, size);
        }
        if (isFourWithTwoSingle(size, countMap)) {
            return buildGroup(CardType.FOUR_WITH_TWO_SINGLE, ranks, countMap, size);
        }
        if (isFourWithTwoPair(size, countMap)) {
            return buildGroup(CardType.FOUR_WITH_TWO_PAIR, ranks, countMap, size);
        }
        if (isStraight(size, ranks)) {
            return buildGroup(CardType.STRAIGHT, ranks, countMap, size);
        }
        if (isConsecutivePairs(size, ranks, countMap)) {
            return buildGroup(CardType.CONSECUTIVE_PAIRS, ranks, countMap, size);
        }
        return new PlayCardGroup(CardType.INVALID, -1, 0);
    }

    /**
     * 构建一个牌组对象。
     *
     * @param type 牌型
     * @param ranks 牌的点数列表
     * @param countMap 每个点数出现次数的映射
     * @param size 牌组中牌的数量
     * @return 包含牌型、主牌点数和牌数组合的分析结果。
     */
    private static PlayCardGroup buildGroup(CardType type, List<Integer> ranks, Map<Integer, Integer> countMap, int size) {
        return new PlayCardGroup(type, extractMainRank(type, ranks, countMap, size), size);
    }

    /**
     * 从牌型和点数统计中提取主牌点数。
     * <p>
     * 主牌点数用于比较同类牌型大小，比如三带一比较三张的点数，四带二比较四张的点数。
     * </p>
     */
    private static int extractMainRank(CardType type, List<Integer> ranks, Map<Integer, Integer> countMap, int size) {
        return switch (type) {
            case SINGLE, PAIR, TRIPLE, BOMB, ROCKET, STRAIGHT, CONSECUTIVE_PAIRS -> maxRank(ranks);
            case THREE_WITH_ONE, THREE_WITH_PAIR -> rankByCount(countMap, 3);
            case FOUR_WITH_TWO_SINGLE, FOUR_WITH_TWO_PAIR -> rankByCount(countMap, 4);
            case AIRPLANE -> lastAirplaneBodyRank(countMap, size / 3, false);
            case AIRPLANE_WITH_SINGLE_WINGS -> lastAirplaneBodyRank(countMap, size / 4, true);
            case AIRPLANE_WITH_PAIR_WINGS -> lastAirplaneBodyRank(countMap, size / 5, true);
            default -> -1;
        };
    }

    /**
     * 返回飞机本体最后一组三张的点数。
     */
    private static int lastAirplaneBodyRank(Map<Integer, Integer> countMap, int bodyGroupCount, boolean allowBombSplit) {
        List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, bodyGroupCount, allowBombSplit);
        return bodyRanks.isEmpty() ? -1 : bodyRanks.getLast();
    }

    /**
     * 找出出现次数等于目标次数的最大点数。
     */
    private static int rankByCount(Map<Integer, Integer> countMap, int targetCount) {
        return countMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == targetCount)
                .map(Map.Entry::getKey)
                .max(Integer::compare)
                .orElse(-1);
    }

    /**
     * 从点数统计中提取飞机本体。
     * <p>
     * 飞机本体必须是连续的三张组合，不能包含 2、小王或大王。
     * </p>
     */
    private static List<Integer> extractAirplaneBodyRanks(Map<Integer, Integer> countMap,
                                                          int bodyGroupCount,
                                                          boolean allowBombSplit) {
        List<Integer> tripleRanks = countMap.entrySet()
                .stream()
                .filter(entry -> allowBombSplit ? entry.getValue() >= 3 : entry.getValue() == 3)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();

        if (tripleRanks.size() < bodyGroupCount) {
            return List.of();
        }

        for (int start = 0; start <= tripleRanks.size() - bodyGroupCount; start++) {
            List<Integer> bodyRanks = tripleRanks.subList(start, start + bodyGroupCount);
            if (isValidAirplaneBody(bodyRanks)) {
                return bodyRanks;
            }
        }
        return List.of();
    }

    /**
     * 判断飞机本体是否连续且没有包含 2 或王。
     */
    private static boolean isValidAirplaneBody(List<Integer> bodyRanks) {
        return isConsecutive(bodyRanks)
                && !bodyRanks.contains(CardUtil.TWO_RANK)
                && !bodyRanks.contains(CardUtil.SMALL_JOKER_RANK)
                && !bodyRanks.contains(CardUtil.BIG_JOKER_RANK);
    }

    /**
     * 检查排序后的点数列表是否连续。
     */
    private static boolean isConsecutive(List<Integer> sortedRanks) {
        for (int i = 1; i < sortedRanks.size(); i++) {
            if (!sortedRanks.get(i).equals(sortedRanks.get(i - 1) + 1)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取一组牌里的最大点数。
     */
    private static int maxRank(List<Integer> ranks) {
        return ranks.stream()
                .max(Integer::compare)
                .orElse(-1);
    }

    /**
     * 判断是否为四带两单。
     */
    private static boolean isFourWithTwoSingle(int size, Map<Integer, Integer> countMap) {
        return size == 6 && countMap.containsValue(4);
    }

    /**
     * 判断是否为四带两对。
     */
    private static boolean isFourWithTwoPair(int size, Map<Integer, Integer> countMap) {
        return size == 8 && countMap.containsValue(4) && countMap.containsValue(2) && !countMap.containsValue(1);
    }

    /**
     * 判断是否为飞机带单张翅膀。
     * <p>
     * 飞机本体不能带 2 或王，但翅膀可以，所以这里先拆出本体，再检查剩余牌。
     * </p>
     */
    private static boolean isAirplaneWithSingleWings(int size, Map<Integer, Integer> countMap) {
        if (size < 8 || size % 4 != 0) {
            return false;
        }

        int bodyGroupCount = size / 4;
        List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, bodyGroupCount, true);
        if (bodyRanks.isEmpty()) {
            return false;
        }

        Map<Integer, Integer> remainingCountMap = removeAirplaneBody(countMap, bodyRanks);
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
     * 判断是否为飞机带对子翅膀。
     */
    private static boolean isAirplaneWithPairWings(int size, Map<Integer, Integer> countMap) {
        if (size < 10 || size % 5 != 0) {
            return false;
        }

        int bodyGroupCount = size / 5;
        List<Integer> bodyRanks = extractAirplaneBodyRanks(countMap, bodyGroupCount, true);
        if (bodyRanks.isEmpty()) {
            return false;
        }

        Map<Integer, Integer> remainingCountMap = removeAirplaneBody(countMap, bodyRanks);
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
     * 从统计结果中扣掉飞机本体，剩下的牌用于检查翅膀。
     */
    private static Map<Integer, Integer> removeAirplaneBody(Map<Integer, Integer> countMap, List<Integer> bodyRanks) {
        Map<Integer, Integer> remainingCountMap = new HashMap<>(countMap);
        for (Integer bodyRank : bodyRanks) {
            remainingCountMap.put(bodyRank, remainingCountMap.get(bodyRank) - 3);
            if (remainingCountMap.get(bodyRank) == 0) {
                remainingCountMap.remove(bodyRank);
            }
        }
        return remainingCountMap;
    }

    /**
     * 判断是否为不带翅膀的飞机。
     */
    private static boolean isAirplane(int size, Map<Integer, Integer> countMap) {
        return size >= 6
                && size % 3 == 0
                && !extractAirplaneBodyRanks(countMap, size / 3, false).isEmpty();
    }

    /**
     * 判断是否为三带一。
     */
    private static boolean isThreeWithOne(int size, Map<Integer, Integer> countMap) {
        return size == 4 && countMap.containsValue(3) && countMap.containsValue(1);
    }

    /**
     * 判断是否为三带二。
     */
    private static boolean isThreeWithPair(int size, Map<Integer, Integer> countMap) {
        return size == 5 && countMap.containsValue(3) && countMap.containsValue(2);
    }

    /**
     * 判断是否为顺子。
     * <p>
     * 顺子要求至少 5 张、不能重复、不能包含 2 或王。
     * </p>
     */
    private static boolean isStraight(int size, List<Integer> ranks) {
        if (size < 5) {
            return false;
        }
        List<Integer> distinctSortedRanks = ranks.stream()
                .distinct()
                .sorted()
                .toList();
        if (distinctSortedRanks.size() != ranks.size()) {
            return false;
        }
        if (containsNonStraightRank(ranks)) {
            return false;
        }
        return isConsecutive(distinctSortedRanks);
    }

    /**
     * 判断是否为连对。
     * <p>
     * 连对要求至少 3 对、每个点数正好两张、不能包含 2 或王。
     * </p>
     */
    private static boolean isConsecutivePairs(int size, List<Integer> ranks, Map<Integer, Integer> countMap) {
        if (size < 6 || containsNonStraightRank(ranks)) {
            return false;
        }
        List<Integer> distinctSortedRanks = ranks.stream()
                .distinct()
                .sorted()
                .toList();
        if (distinctSortedRanks.size() == ranks.size()) {
            return false;
        }
        for (Integer rank : distinctSortedRanks) {
            if (!countMap.get(rank).equals(2)) {
                return false;
            }
        }

        return isConsecutive(distinctSortedRanks);
    }

    /**
     * 顺子和连对里不能出现 2、小王或大王。
     */
    private static boolean containsNonStraightRank(List<Integer> ranks) {
        return ranks.contains(CardUtil.TWO_RANK)
                || ranks.contains(CardUtil.SMALL_JOKER_RANK)
                || ranks.contains(CardUtil.BIG_JOKER_RANK);
    }

    /**
     * 根据牌ID获取点数。
     * <p>
     * 普通牌ID为 0 到 51，52 是小王，53 是大王。
     * </p>
     */
    private static int getRank(int cardId) {
        if (cardId < 0 || cardId > 53) {
            throw new IllegalArgumentException("非法牌索引: " + cardId);
        }
        if (cardId <= 51) {
            return cardId / 4 + 1;
        }
        return cardId == 52 ? CardUtil.SMALL_JOKER_RANK : CardUtil.BIG_JOKER_RANK;
    }
}
