package util;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardUtilTest {

    @Test
    void shouldReturnEmptyListWhenInputIsPass() {
        Collection<Integer> handCards = List.of(1, 2, 3, 4, 5);

        Collection<Integer> result = CardUtil.stringToCards("pass", handCards);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenInputIsUppercasePassWithSpaces() {
        Collection<Integer> handCards = List.of(1, 2, 3, 4, 5);

        Collection<Integer> result = CardUtil.stringToCards("  PASS  ", handCards);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenInputIsBlank() {
        Collection<Integer> handCards = List.of(1, 2, 3, 4, 5);

        Collection<Integer> result = CardUtil.stringToCards("   ", handCards);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldParse3334WithoutSpaces() {
        // 1~4 是四张 3，5~8 是四张 4
        Collection<Integer> handCards = List.of(1, 2, 3, 5, 9, 10);

        Collection<Integer> result = CardUtil.stringToCards("3334", handCards);

        assertNotNull(result);
        assertEquals(4, result.size());
        // 应该取到三张3和一张4
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
        assertTrue(result.contains(5));
    }

    @Test
    void shouldParse3334WithSpaces() {
        Collection<Integer> handCards = List.of(1, 2, 3, 5, 9, 10);

        Collection<Integer> result = CardUtil.stringToCards("3 3 3 4", handCards);

        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
        assertTrue(result.contains(5));
    }

    @Test
    void shouldParse10JQ() {
        // 29~32 是 10，33~36 是 J，37~40 是 Q
        Collection<Integer> handCards = List.of(29, 33, 37, 1, 2);

        Collection<Integer> result = CardUtil.stringToCards("10JQ", handCards);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(29));
        assertTrue(result.contains(33));
        assertTrue(result.contains(37));
    }

    @Test
    void shouldParse10JQWithSpaces() {
        Collection<Integer> handCards = List.of(29, 33, 37, 1, 2);

        Collection<Integer> result = CardUtil.stringToCards("10 J Q", handCards);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(29));
        assertTrue(result.contains(33));
        assertTrue(result.contains(37));
    }

    @Test
    void shouldParseJQKAWithoutSpaces() {
        Collection<Integer> handCards = List.of(32, 36, 40, 44, 1, 2);

        Collection<Integer> result = CardUtil.stringToCards("JQKA", handCards);

        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(32));
        assertTrue(result.contains(36));
        assertTrue(result.contains(40));
        assertTrue(result.contains(44));
    }

    @Test
    void shouldParseMixedWhitespaceBetweenCards() {
        Collection<Integer> handCards = List.of(0, 4, 8, 12);

        Collection<Integer> result = CardUtil.stringToCards("3\t4  5\n6", handCards);

        assertNotNull(result);
        assertEquals(List.of(0, 4, 8, 12), result.stream().toList());
    }

    @Test
    void shouldParseSmallAndBigJoker() {
        // 53 = 小王, 54 = 大王
        Collection<Integer> handCards = List.of(52, 53, 0, 1);

        Collection<Integer> result = CardUtil.stringToCards("小王大王", handCards);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(52));
        assertTrue(result.contains(53));
    }

    @Test
    void shouldFormatCardsToReadableString() {
        assertEquals("3⬛️ 3♣️ 小王 大王", CardUtil.cardsToString(List.of(0, 1, 52, 53)));
    }

    @Test
    void shouldThrowWhenCardsToStringReceivesUnknownCardId() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardUtil.cardsToString(List.of(54))
        );

        assertEquals("未知的牌号: 54", ex.getMessage());
    }

    @Test
    void shouldThrowWhenCardsToStringReceivesNullCardId() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> {
                    List<Integer> cards = new ArrayList<>();
                    cards.add(1);
                    cards.add(null);
                    CardUtil.cardsToString(cards);
                }
        );

        assertEquals("cardIds 中不能包含空牌号", ex.getMessage());
    }

    @Test
    void shouldThrowWhenHandDoesNotContainEnoughCards() {
        Collection<Integer> handCards = List.of(1, 2, 5);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardUtil.stringToCards("3334", handCards)
        );

        assertTrue(ex.getMessage()
                .contains("手牌中没有足够的"));
    }

    @Test
    void shouldThrowWhenInputContainsIllegalToken() {
        Collection<Integer> handCards = List.of(1, 2, 3, 4);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardUtil.stringToCards("3X4", handCards)
        );

        assertEquals("不是合法的扑克牌输入", ex.getMessage());
    }

    @Test
    void shouldThrowWhenInputIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardUtil.stringToCards(null, List.of(1, 2, 3))
        );

        assertEquals("字符串不能为空", ex.getMessage());
    }

    @Test
    void shouldThrowWhenPlayerHandCardsIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> CardUtil.stringToCards("3", null)
        );

        assertEquals("玩家手牌不能为空", ex.getMessage());
    }

    @Test
    void shouldPreferDifferentConcreteCardsForDuplicateRanks() {
        Collection<Integer> handCards = List.of(1, 2, 3, 4);

        Collection<Integer> result = CardUtil.stringToCards("333", handCards);

        assertNotNull(result);
        assertEquals(3, result.size());
        // 不能重复用同一张牌
        assertEquals(3, result.stream()
                .distinct()
                .count());
    }
}
