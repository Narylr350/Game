package game.action;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ActionType 单元测试类。
 */
class ActionTypeTest {

    @Test
    void testFromString_Call() {
        ActionType result = ActionType.fromString("叫");
        assertEquals(ActionType.CALL, result);
    }

    @Test
    void testFromString_Pass() {
        ActionType result = ActionType.fromString("不叫");
        assertEquals(ActionType.PASS, result);
    }

    @Test
    void testFromString_NullInput() {
        ActionType result = ActionType.fromString(null);
        assertNull(result);
    }

    @Test
    void testFromString_InvalidInput() {
        ActionType result = ActionType.fromString("无效");
        assertNull(result);
    }

    @Test
    void testFromString_EmptyString() {
        ActionType result = ActionType.fromString("");
        assertNull(result);
    }

    @Test
    void testFromString_Whitespace() {
        ActionType result = ActionType.fromString("  ");
        assertNull(result);
    }

    @Test
    void testFromString_CaseSensitive() {
        // 中文输入应该精确匹配
        ActionType result = ActionType.fromString("叫");
        assertEquals(ActionType.CALL, result);
    }

    @Test
    void testGetText_Call() {
        assertEquals("叫", ActionType.CALL.getText());
    }

    @Test
    void testGetText_Pass() {
        assertEquals("不叫", ActionType.PASS.getText());
    }

    @Test
    void testValues_Count() {
        assertEquals(2, ActionType.values().length);
    }
}
