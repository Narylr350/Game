package game.model;

import game.enumtype.GameEventType;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameResultTest {

    @Test
    void should_create_rejected_result_for_target_player() {
        GameResult result = GameResult.rejected("不能操作", 2);

        assertFalse(result.isSuccess());
        assertTrue(result.isRejected());
        assertEquals("不能操作", result.getMessage());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertEquals(2, result.getSendToPlayerId());
        assertTrue(result.getPlayerMessages().isEmpty());
        assertNull(result.getWinnerPlayerId());
    }

    @Test
    void should_create_accepted_result() {
        GameResult result = GameResult.accepted("出牌");

        assertTrue(result.isSuccess());
        assertTrue(result.isAccepted());
        assertEquals("出牌", result.getMessage());
        assertEquals(GameEventType.ACTION_ACCEPTED, result.getEventType());
        assertNull(result.getSendToPlayerId());
    }

    @Test
    void should_create_landlord_decided_result() {
        GameResult result = GameResult.landlordDecided("地主确认：玩家 1");

        assertTrue(result.isSuccess());
        assertTrue(result.isLandlordDecided());
        assertEquals(GameEventType.LANDLORD_DECIDED, result.getEventType());
        assertEquals("地主确认：玩家 1", result.getMessage());
    }

    @Test
    void should_create_redeal_required_result() {
        GameResult result = GameResult.redealRequired("三人都不叫地主，重新发牌");

        assertTrue(result.isSuccess());
        assertTrue(result.isRedealRequired());
        assertEquals(GameEventType.REDEAL_REQUIRED, result.getEventType());
        assertEquals("三人都不叫地主，重新发牌", result.getMessage());
    }

    @Test
    void should_create_game_settled_result_with_winner_and_immutable_player_messages() {
        Map<Integer, String> messages = new LinkedHashMap<>();
        messages.put(1, "地主胜利");
        messages.put(2, "农民失败");

        GameResult result = GameResult.gameSettled("游戏结束", messages, 1);
        messages.put(3, "农民失败");

        assertTrue(result.isSuccess());
        assertTrue(result.isGameSettled());
        assertEquals(GameEventType.GAME_SETTLED, result.getEventType());
        assertEquals("游戏结束", result.getMessage());
        assertEquals(1, result.getWinnerPlayerId());
        assertEquals(Map.of(1, "地主胜利", 2, "农民失败"), result.getPlayerMessages());
        assertThrows(UnsupportedOperationException.class, () -> result.getPlayerMessages().put(4, "不能改"));
    }
}
