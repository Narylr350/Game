package game.handler;

import game.GameEventType;
import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CallLandlordHandler 单元测试类。
 */
class CallLandlordHandlerTest {

    private CallLandlordHandler handler;
    private GameRoom room;

    @BeforeEach
    void setUp() {
        handler = new CallLandlordHandler();
        room = createTestRoom();
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        room.setCurrentPlayerId(1);
    }

    @Test
    void testHandle_CallAction_Success() {
        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertTrue(result.isSuccess());
        assertEquals(GameEventType.ACTION_ACCEPTED, result.getEventType());
        assertEquals(GamePhase.ROB_LANDLORD, room.getCurrentPhase());
        assertEquals(1, room.getLandlordCandidateId());
        assertEquals(1, room.getFirstCallerId());
        assertEquals(2, room.getCurrentPlayerId());
    }

    @Test
    void testHandle_PassAction_Success() {
        GameAction action = new GameAction(1, ActionType.PASS, null);
        GameResult result = handler.handle(room, action);

        assertTrue(result.isSuccess());
        assertEquals(GameEventType.ACTION_ACCEPTED, result.getEventType());
        assertEquals(2, room.getCurrentPlayerId());
        assertEquals(1, room.getCallPassCount());
        assertTrue(room.getCallPassPlayerIds().contains(1));
    }

    @Test
    void testHandle_PassAction_AllThreePass_Redeal() {
        // 玩家1不叫
        handler.handle(room, new GameAction(1, ActionType.PASS, null));
        // 玩家2不叫
        handler.handle(room, new GameAction(2, ActionType.PASS, null));
        // 玩家3不叫，触发重发
        GameResult result = handler.handle(room, new GameAction(3, ActionType.PASS, null));

        assertTrue(result.isSuccess());
        assertEquals(GameEventType.REDEAL_REQUIRED, result.getEventType());
        assertEquals(0, room.getCallPassCount());
    }

    @Test
    void testHandle_InvalidActionType() {
        GameAction action = new GameAction(1, null, null);
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertEquals(1, result.getSendToPlayerId());
    }

    @Test
    void testHandle_WrongPlayerId() {
        GameAction action = new GameAction(2, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
    }

    @Test
    void testHandle_WrongPhase() {
        room.setCurrentPhase(GamePhase.PLAYING);
        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
    }

    @Test
    void testHandle_HasLandlord_AlreadySet() {
        room.setLandlordPlayerId(1);
        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
    }

    @Test
    void testHandle_CurrentPlayerIdIsNull() {
        room.setCurrentPlayerId(null);
        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
    }

    @Test
    void testHandle_MultipleCalls_ChainCorrectly() {
        // 玩家1不叫
        handler.handle(room, new GameAction(1, ActionType.PASS, null));
        assertEquals(2, room.getCurrentPlayerId());

        // 玩家2叫地主
        GameResult result = handler.handle(room, new GameAction(2, ActionType.CALL, null));
        assertTrue(result.isSuccess());
        assertEquals(GamePhase.ROB_LANDLORD, room.getCurrentPhase());
        assertEquals(2, room.getLandlordCandidateId());
        assertEquals(3, room.getCurrentPlayerId());
    }

    private GameRoom createTestRoom() {
        var players = Arrays.asList(
            new PlayerState(1, "Player1", new TreeSet<>(Arrays.asList(1, 2, 3))),
            new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(4, 5, 6))),
            new PlayerState(3, "Player3", new TreeSet<>(Arrays.asList(7, 8, 9)))
        );
        return new GameRoom(players, new TreeSet<>());
    }
}
