package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

// 纯工具类：负责牌堆模板和牌面显示，不参与对局流程控制。
public final class CardUtil {
    /**
     * 卡牌字典，用于存储卡牌ID与对应卡牌名称的映射。
     * 该映射表以整数作为键（代表卡牌ID），字符串作为值（代表卡牌的具体描述或名称）。
     * 主要用于将卡牌ID转换为易于理解的文本形式，或者反之，确保了卡牌信息的一致性和可读性。
     */
    private static final Map<Integer, String> CARD_DICTIONARY = new HashMap<>();
    /**
     * 一副牌的模板,包含54张牌,每张牌用唯一的整数ID表示。
     * 该模板用于生成标准的扑克牌堆,其中包含了所有可能的牌,包括大小王。
     * 模板中的牌顺序是固定的,通常用于创建新的、未洗牌的牌堆。
     */
    private static final List<Integer> DECK_TEMPLATE = new ArrayList<>();

    public static final int TWO_RANK = 13;
    public static final int SMALL_JOKER_RANK = 14;
    public static final int BIG_JOKER_RANK = 15;

    static {
        List<String> numbers = new ArrayList<>();
        List<String> suits = new ArrayList<>();
        List<String> jokers = new ArrayList<>();
        List<String> cards = new ArrayList<>();

        Collections.addAll(numbers, "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2");
        Collections.addAll(suits, "⬛️", "♣️", "♥️", "♠️");
        Collections.addAll(jokers, "小王", "大王");

        for (String number : numbers) {
            for (String suit : suits) {
                cards.add(number + suit);
            }
        }
        cards.addAll(jokers);

        for (int i = 0; i < cards.size(); i++) {
            CARD_DICTIONARY.put(i, cards.get(i));
            DECK_TEMPLATE.add(i);
        }
    }

    private CardUtil() {
    }

    /**
     * 创建一副已洗牌的牌堆。
     * <p>
     * 该方法基于预定义的牌堆模板(54张牌)创建一个新牌堆,并对其进行随机洗牌。
     * 每次调用都会返回一个独立的、随机排序的牌堆实例。
     * </p>
     *
     * @return 包含1-54的整数列表,表示已洗牌的牌堆,每张牌用唯一ID表示
     */
    public static List<Integer> createShuffledDeck() {
        List<Integer> deck = new ArrayList<>(DECK_TEMPLATE);
        Collections.shuffle(deck);
        return deck;
    }

    /**
     * 将牌ID集合转换为可读的牌面字符串。
     * <p>
     * 该方法将内部的牌ID(1-54)转换为对应的牌面显示文本(如"3⬛️ 4♣️ 大王")。
     * 会对输入进行严格校验,拒绝null值、空集合中的null元素以及未知的牌ID。
     * </p>
     *
     * @param cardIds 牌ID集合,每个ID必须在1-54范围内且不能为null
     * @return 用空格分隔的牌面显示字符串
     * @throws IllegalArgumentException 如果cardIds为null,或包含null元素,或包含未知的牌ID
     */
    public static String cardsToString(Collection<Integer> cardIds) {
        if (cardIds == null) {
            throw new IllegalArgumentException("cardIds 不能为空");
        }

        return cardIds.stream()
                .map(cardId -> {
                    if (cardId == null) {
                        throw new IllegalArgumentException("cardIds 中不能包含空牌号");
                    }

                    String card = CARD_DICTIONARY.get(cardId);
                    if (card == null) {
                        throw new IllegalArgumentException("未知的牌号: " + cardId);
                    }
                    return card;
                })
                .collect(Collectors.joining(" "));
    }

    /**
     * 把玩家输入的牌面字符串，解析成“该玩家手牌中的具体牌索引”。
     * <p>
     * 规则：
     * 1. 空串或 pass 返回空列表，表示不出。
     * 2. 支持无空格输入：3334 / 10JQ / 小王大王
     * 3. 支持有空格输入：3 3 3 4 / 10 J Q
     * 4. 只校验玩家手里是否真的有这些牌，不校验牌型是否合法。
     *
     * @param cards           玩家输入
     * @param playerHandCards 玩家当前手牌索引
     * @return 要出的具体牌索引列表
     */
    public static Collection<Integer> stringToCards(String cards, Collection<Integer> playerHandCards) {
        if (cards == null) {
            throw new IllegalArgumentException("字符串不能为空");
        }

        cards = cards.trim().toUpperCase();
        if (cards.isEmpty() || "PASS".equalsIgnoreCase(cards)) {
            return new ArrayList<>();
        }

        cards = cards.replace(" ", "");

        List<String> inputTokens = tokenize(cards);
        if (inputTokens.isEmpty()) {
            throw new IllegalArgumentException("不是合法的扑克牌输入");
        }

        List<Integer> available = new ArrayList<>(playerHandCards);
        List<Integer> result = new ArrayList<>();

        for (String token : inputTokens) {
            Integer matchedCardId = findAndRemoveFirstMatchedCard(token, available);
            if (matchedCardId == null) {
                throw new IllegalArgumentException("手牌中没有足够的 " + token);
            }
            result.add(matchedCardId);
        }

        return result;
    }

    /**
     * 把字符串拆成 token。
     * 例：
     * 3334 -> [3,3,3,4]
     * 10JQ -> [10,J,Q]
     * 小王大王 -> [小王,大王]
     */
    public static List<String> tokenize(String cards) {
        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < cards.length()) {
            String matched = matchLongestTokenPrefix(cards, i);
            if (matched == null) {
                return Collections.emptyList();
            }
            result.add(matched);
            i += matched.length();
        }

        return result;
    }

    /**
     * 从给定的字符串中，找到从指定位置开始的最长词元前缀。
     * 该方法会尝试匹配预定义的词元列表，并返回最长的匹配项。
     *
     * @param cards 要解析的牌面字符串
     * @param start 开始查找的位置
     * @return 找到的最长词元前缀，如果未找到则返回null
     */
    private static String matchLongestTokenPrefix(String cards, int start) {
        // 最长优先，避免 10 被拆成 1 和 0
        List<String> orderedTokens = List.of("小王", "大王", "10", "J", "Q", "K", "A", "2", "3", "4", "5", "6", "7", "8", "9");
        for (String token : orderedTokens) {
            if (cards.startsWith(token, start)) {
                return token;
            }
        }
        return null;
    }

    /**
     * 从可用牌列表中查找并移除第一个与给定词元匹配的牌。
     *
     * @param token     要匹配的词元
     * @param available 可用牌ID列表
     * @return 找到并移除的第一个匹配牌的ID，如果没有找到匹配项则返回null
     */
    private static Integer findAndRemoveFirstMatchedCard(String token, List<Integer> available) {
        for (int i = 0; i < available.size(); i++) {
            Integer cardId = available.get(i);
            String cardText = CARD_DICTIONARY.get(cardId);
            if (cardText == null) {
                continue;
            }

            if (matchesToken(cardText, token)) {
                available.remove(i);
                return cardId;
            }
        }
        return null;
    }

    /**
     * 检查给定的牌面文本是否与词元匹配。
     *
     * @param cardText 牌面显示文本
     * @param token    要匹配的词元
     * @return 如果牌面文本与词元匹配，则返回true；否则返回false
     */
    private static boolean matchesToken(String cardText, String token) {
        if ("小王".equals(token) || "大王".equals(token)) {
            return cardText.equals(token);
        }
        return cardText.startsWith(token);
    }
}