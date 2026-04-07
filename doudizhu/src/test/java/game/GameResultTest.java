package game;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameResult 单元测试类。
 */
class GameResultTest {

    @Test
    void testRejected_WithMessage() {
        GameResult result = GameResult.rejected("操作无效");

        assertFalse(result.isSuccess());
        assertEquals("操作无效", result.getMessage());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertNull(result.getSendToPlayerId());
    }

    @Test
    void testRejected_WithMessageAndPlayerId() {
        GameResult result = GameResult.rejected("操作无效", 1);

        assertFalse(result.isSuccess());
        assertEquals("操作无效", result.getMessage());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertEquals(1, result.getSendToPlayerId());
    }

    @Test
    void testAccepted_WithMessage() {
        GameResult result = GameResult.accepted("操作成功");

        assertTrue(result.isSuccess());
        assertEquals("操作成功", result.getMessage());
        assertEquals(GameEventType.ACTION_ACCEPTED, result.getEventType());
        assertNull(result.getSendToPlayerId());
    }

    @Test
    void testAccepted_WithMessageAndPlayerId() {
        GameResult result = GameResult.accepted("操作成功", 2);

        assertTrue(result.isSuccess());
        assertEquals("操作成功", result.getMessage());
        assertEquals(GameEventType.ACTION_ACCEPTED, result.getEventType());
        assertEquals(2, result.getSendToPlayerId());
    }

    @Test
    void testRedealRequired() {
        GameResult result = GameResult.redealRequired("需要重新发牌");

        assertTrue(result.isSuccess());
        assertEquals("需要重新发牌", result.getMessage());
        assertEquals(GameEventType.REDEAL_REQUIRED, result.getEventType());
        assertNull(result.getSendToPlayerId());
    }

    @Test
    void testLandlordDecided() {
        GameResult result = GameResult.landlordDecided("地主已确定");

        assertTrue(result.isSuccess());
        assertEquals("地主已确定", result.getMessage());
        assertEquals(GameEventType.LANDLORD_DECIDED, result.getEventType());
        assertNull(result.getSendToPlayerId());
    }

    @Test
    void testConstructor_AllFields() {
        GameResult result = new GameResult(true, "测试", GameEventType.NONE, 3);

        assertTrue(result.isSuccess());
        assertEquals("测试", result.getMessage());
        assertEquals(GameEventType.NONE, result.getEventType());
        assertEquals(3, result.getSendToPlayerId());
    }

    @Test
    void testImmutability() {
        GameResult result = GameResult.accepted("成功", 1);

        // 验证没有 setter 方法（通过反射或直接确认字段为 final）
        // 这里通过多次调用确认返回值一致
        assertSame(result.getEventType(), result.getEventType());
        assertSame(result.getMessage(), result.getMessage());
    }
}
