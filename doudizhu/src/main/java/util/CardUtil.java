package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    /**
     * 一个映射，将不含花色的牌ID映射到对应的牌面字符串。
     * 此映射用于将数字形式的牌ID转换为人类可读的牌面表示，
     * 但不包含花色信息。例如，"3"代表所有花色的3。
     */
    private static final Map<Integer,String> CARD_DICTIONARY_WITHOUTSUITS = new HashMap<>();

    static {
        List<String> numbers = new ArrayList<>();
        List<String> suits = new ArrayList<>();
        List<String> cards = new ArrayList<>();
        List<String> cardsWithoutSuits = new ArrayList<>();

        Collections.addAll(numbers, "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2");
        Collections.addAll(suits, "⬛️", "♣️", "♥️", "♠️");

        for (String number : numbers) {
            for (String suit : suits) {
                cards.add(number + suit);
            }
        }

        for (String number : numbers) {
            for (int i = 0; i < 4; i++) {
                cardsWithoutSuits.add(number);
            }
        }

        cards.add("小王");
        cards.add("大王");
        cardsWithoutSuits.add("小王");
        cardsWithoutSuits.add("大王");

        for (int i = 0; i < cards.size(); i++) {
            CARD_DICTIONARY.put(i + 1, cards.get(i));
            DECK_TEMPLATE.add(i + 1);
        }
        for (int i = 0; i < cardsWithoutSuits.size(); i++) {
            CARD_DICTIONARY_WITHOUTSUITS.put(i + 1, cardsWithoutSuits.get(i));
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

    public static Collection<Integer> stringToCards(String cards){
        if (cards.isBlank()){
            throw new IllegalArgumentException("字符串不能为空");
        }
        return Stream.of(cards).map(new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                String[] split = s.split(" ");
                if (s.isBlank()){

                }
                return 0;
            }
        }).toList();
    }
}
