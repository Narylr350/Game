package game.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlayState 单元测试类。
 */
class PlayStateTest {

    private PlayState playState;

    @BeforeEach
    void setUp() {
        playState = new PlayState();
    }

    @Test
    void testGetLastPlayPlayerId_DefaultNull() {
        assertNull(playState.getLastPlayPlayerId());
    }

    @Test
    void testSetLastPlayPlayerId() {
        playState.setLastPlayPlayerId(1);
        assertEquals(1, playState.getLastPlayPlayerId());
    }

    @Test
    void testGetLastPlayedCards_DefaultNull() {
        assertNull(playState.getLastPlayedCards());
    }

    @Test
    void testSetLastPlayedCards() {
        List<Integer> cards = Arrays.asList(1, 2, 3);
        playState.setLastPlayedCards(cards);
        assertEquals(cards, playState.getLastPlayedCards());
    }

    @Test
    void testGetPassCount_DefaultZero() {
        assertEquals(0, playState.getPassCount());
    }

    @Test
    void testSetPassCount() {
        playState.setPassCount(5);
        assertEquals(5, playState.getPassCount());
    }

    @Test
    void testAddPassCount() {
        playState.addPassCount();
        playState.addPassCount();
        playState.addPassCount();
        assertEquals(3, playState.getPassCount());
    }

    @Test
    void testResetRound_ClearsAllState() {
        playState.setLastPlayPlayerId(1);
        playState.setLastPlayedCards(Arrays.asList(1, 2, 3));
        playState.setPassCount(2);

        playState.resetRound();

        assertNull(playState.getLastPlayPlayerId());
        assertNull(playState.getLastPlayedCards());
        assertEquals(0, playState.getPassCount());
    }

    @Test
    void testResetRound_WhenAlreadyEmpty() {
        playState.resetRound();

        assertNull(playState.getLastPlayPlayerId());
        assertNull(playState.getLastPlayedCards());
        assertEquals(0, playState.getPassCount());
    }

    @Test
    void testFullRound_Scenario() {
        // 模拟一轮出牌
        playState.setLastPlayPlayerId(1);
        playState.setLastPlayedCards(Arrays.asList(1, 2));
        playState.addPassCount(); // 玩家2过牌
        playState.addPassCount(); // 玩家3过牌

        assertEquals(1, playState.getLastPlayPlayerId());
        assertEquals(2, playState.getPassCount());

        // 一轮结束，重置
        playState.resetRound();

        assertNull(playState.getLastPlayPlayerId());
        assertEquals(0, playState.getPassCount());
    }
}
