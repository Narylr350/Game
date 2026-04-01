package util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CardUtilTest {

    @Test
    void createShuffledDeckReturnsAllFiftyFourUniqueCards() {
        List<Integer> deck = CardUtil.createShuffledDeck();

        Set<Integer> uniqueCards = new HashSet<>(deck);

        assertEquals(54, deck.size());
        assertEquals(54, uniqueCards.size());
    }

    @Test
    void cardsToStringRendersHumanReadableCards() {
        String cards = CardUtil.cardsToString(List.of(1, 53, 54));

        assertFalse(cards.isBlank());
        assertTrue(cards.contains("3"));
        assertTrue(cards.contains("小王"));
        assertTrue(cards.contains("大王"));
    }
}
