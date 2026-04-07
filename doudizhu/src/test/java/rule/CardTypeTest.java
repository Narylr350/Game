package rule;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CardType 单元测试类。
 */
class CardTypeTest {

    @Test
    void testCardType_Values() {
        CardType[] values = CardType.values();
        assertEquals(9, values.length);
    }

    @Test
    void testCardType_Invalid() {
        assertEquals("INVALID", CardType.INVALID.name());
    }

    @Test
    void testCardType_Single() {
        assertEquals("SINGLE", CardType.SINGLE.name());
    }

    @Test
    void testCardType_Pair() {
        assertEquals("PAIR", CardType.PAIR.name());
    }

    @Test
    void testCardType_Triple() {
        assertEquals("TRIPLE", CardType.TRIPLE.name());
    }

    @Test
    void testCardType_Straight() {
        assertEquals("STRAIGHT", CardType.STRAIGHT.name());
    }

    @Test
    void testCardType_ConsecutivePairs() {
        assertEquals("CONSECUTIVE_PAIRS", CardType.CONSECUTIVE_PAIRS.name());
    }

    @Test
    void testCardType_Airplane() {
        assertEquals("AIRPLANE", CardType.AIRPLANE.name());
    }

    @Test
    void testCardType_Bomb() {
        assertEquals("BOMB", CardType.BOMB.name());
    }

    @Test
    void testCardType_Rocket() {
        assertEquals("ROCKET", CardType.ROCKET.name());
    }

    @Test
    void testCardType_ValueOf() {
        assertEquals(CardType.SINGLE, CardType.valueOf("SINGLE"));
        assertEquals(CardType.BOMB, CardType.valueOf("BOMB"));
        assertEquals(CardType.ROCKET, CardType.valueOf("ROCKET"));
    }
}
