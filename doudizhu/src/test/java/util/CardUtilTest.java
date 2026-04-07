package util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CardUtil 单元测试类。
 */
class CardUtilTest {

    @Test
    void testCreateShuffledDeck_Returns54Cards() {
        List<Integer> deck = CardUtil.createShuffledDeck();
        
        assertNotNull(deck);
        assertEquals(54, deck.size());
    }

    @Test
    void testCreateShuffledDeck_ContainsAllCards() {
        List<Integer> deck = CardUtil.createShuffledDeck();
        Set<Integer> cardSet = new HashSet<>(deck);
        
        assertEquals(54, cardSet.size());
        for (int i = 1; i <= 54; i++) {
            assertTrue(cardSet.contains(i), "牌堆应包含牌ID: " + i);
        }
    }

    @Test
    void testCreateShuffledDeck_ReturnsDifferentOrder() {
        List<Integer> deck1 = CardUtil.createShuffledDeck();
        List<Integer> deck2 = CardUtil.createShuffledDeck();
        
        // 两次洗牌应该有不同顺序(概率极小会相同)
        assertNotEquals(deck1, deck2, "两次洗牌应该产生不同的顺序");
    }

    @Test
    void testCreateShuffledDeck_ReturnsIndependentInstances() {
        List<Integer> deck1 = CardUtil.createShuffledDeck();
        List<Integer> deck2 = CardUtil.createShuffledDeck();
        
        // 修改deck1不应影响deck2
        deck1.clear();
        assertFalse(deck2.isEmpty(), "返回的牌堆实例应该是独立的");
    }

    @Test
    void testCardsToString_ValidCards() {
        List<Integer> cards = Arrays.asList(1, 2, 54);
        String result = CardUtil.cardsToString(cards);
        
        assertNotNull(result);
        assertTrue(result.contains("3⬛️"));
        assertTrue(result.contains("3♣️"));
        assertTrue(result.contains("大王"));
    }

    @Test
    void testCardsToString_SingleCard() {
        List<Integer> cards = Arrays.asList(53);
        String result = CardUtil.cardsToString(cards);
        
        assertEquals("小王", result);
    }

    @Test
    void testCardsToString_EmptyList() {
        List<Integer> cards = new ArrayList<>();
        String result = CardUtil.cardsToString(cards);
        
        assertEquals("", result);
    }

    @Test
    void testCardsToString_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> 
            CardUtil.cardsToString(null)
        );
    }

    @Test
    void testCardsToString_ContainsNullElement() {
        List<Integer> cards = new ArrayList<>();
        cards.add(1);
        cards.add(null);
        
        assertThrows(IllegalArgumentException.class, () -> 
            CardUtil.cardsToString(cards)
        );
    }

    @Test
    void testCardsToString_InvalidCardId() {
        List<Integer> cards = Arrays.asList(0);
        
        assertThrows(IllegalArgumentException.class, () -> 
            CardUtil.cardsToString(cards)
        );
    }

    @Test
    void testCardsToString_InvalidCardId_TooLarge() {
        List<Integer> cards = Arrays.asList(55);
        
        assertThrows(IllegalArgumentException.class, () -> 
            CardUtil.cardsToString(cards)
        );
    }

    @Test
    void testCardsToString_MultipleValidCards() {
        List<Integer> cards = Arrays.asList(1, 5, 9, 13, 53, 54);
        String result = CardUtil.cardsToString(cards);
        
        String[] cardArray = result.split(" ");
        assertEquals(6, cardArray.length);
    }
}
