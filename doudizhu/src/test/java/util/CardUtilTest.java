package util;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CardUtilTest {

    @Test
    void createShuffledDeckReturnsAllFiftyFourUniqueCards() {
        List<Integer> deck = CardUtil.createShuffledDeck();

        Set<Integer> expectedCards = IntStream.rangeClosed(1, 54)
                .boxed()
                .collect(Collectors.toSet());

        assertEquals(expectedCards, Set.copyOf(deck));
    }

    @Test
    void cardsToStringRendersHumanReadableCards() {
        String cards = CardUtil.cardsToString(List.of(1, 53, 54));

        assertFalse(cards.isBlank());
        assertEquals("3⬛️ 小王 大王", cards);
    }
}
