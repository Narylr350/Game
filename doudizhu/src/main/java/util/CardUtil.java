package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CardUtil {
    private static final Map<Integer, String> CARD_DICTIONARY = new HashMap<>();
    private static final List<Integer> DECK_TEMPLATE = new ArrayList<>();

    static {
        List<String> numbers = new ArrayList<>();
        List<String> suits = new ArrayList<>();
        List<String> cards = new ArrayList<>();

        Collections.addAll(numbers, "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2");
        Collections.addAll(suits, "⬛️", "♣️", "♥️", "♠️");

        for (String number : numbers) {
            for (String suit : suits) {
                cards.add(number + suit);
            }
        }

        cards.add("小王");
        cards.add("大王");

        for (int i = 0; i < cards.size(); i++) {
            CARD_DICTIONARY.put(i + 1, cards.get(i));
            DECK_TEMPLATE.add(i + 1);
        }
    }

    private CardUtil() {
    }

    public static List<Integer> createShuffledDeck() {
        List<Integer> deck = new ArrayList<>(DECK_TEMPLATE);
        Collections.shuffle(deck);
        return deck;
    }

    public static String cardsToString(Collection<Integer> cardIds) {
        return cardIds.stream()
                .map(CARD_DICTIONARY::get)
                .collect(Collectors.joining(" "));
    }
}
