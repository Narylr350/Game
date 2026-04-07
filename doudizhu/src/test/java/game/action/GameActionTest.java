package game.action;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameAction 单元测试类。
 */
class GameActionTest {

    @Test
    void testConstructor_ValidParameters() {
        List<Integer> cards = Arrays.asList(1, 2, 3);
        GameAction action = new GameAction(1, ActionType.CALL, cards);
        
        assertEquals(1, action.getPlayerId());
        assertEquals(ActionType.CALL, action.getType());
        assertEquals(cards, action.getCards());
    }

    @Test
    void testGetPlayerId() {
        GameAction action = new GameAction(5, ActionType.PASS, null);
        assertEquals(5, action.getPlayerId());
    }

    @Test
    void testGetType_Call() {
        GameAction action = new GameAction(1, ActionType.CALL, null);
        assertEquals(ActionType.CALL, action.getType());
    }

    @Test
    void testGetType_Pass() {
        GameAction action = new GameAction(1, ActionType.PASS, null);
        assertEquals(ActionType.PASS, action.getType());
    }

    @Test
    void testGetCards_WithCards() {
        List<Integer> cards = Arrays.asList(1, 5, 9);
        GameAction action = new GameAction(1, ActionType.CALL, cards);
        
        assertEquals(cards, action.getCards());
    }

    @Test
    void testGetCards_Null() {
        GameAction action = new GameAction(1, ActionType.PASS, null);
        assertNull(action.getCards());
    }

    @Test
    void testGetCards_EmptyList() {
        List<Integer> cards = Collections.emptyList();
        GameAction action = new GameAction(1, ActionType.CALL, cards);
        
        assertTrue(action.getCards().isEmpty());
    }

    @Test
    void testGetCards_ReturnsSameReference() {
        List<Integer> cards = Arrays.asList(1, 2, 3);
        GameAction action = new GameAction(1, ActionType.CALL, cards);
        
        // GameAction 应该返回相同的引用(不可变)
        assertSame(cards, action.getCards());
    }

    @Test
    void testActionWithMultipleCards() {
        List<Integer> cards = Arrays.asList(1, 2, 3, 4, 5);
        GameAction action = new GameAction(2, ActionType.CALL, cards);
        
        assertEquals(2, action.getPlayerId());
        assertEquals(ActionType.CALL, action.getType());
        assertEquals(5, action.getCards().size());
    }
}
