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
 * RobLandlordHandler 单元测试类。
 */
class RobLandlordHandlerTest {

    private RobLandlordHandler handler;
    private GameRoom room;

    @BeforeEach
    void setUp() {
        handler = new RobLandlordHandler();
        room = createTestRoom();
        room.setCurrentPhase(GamePhase.ROB_LANDLORD);
        room.setCurrentPlayerId(2); // 从玩家2开始，玩家1是第一个叫地主的
        room.setFirstCallerId(1);
    }

    @Test
    void testHandle_CallAction_Success() {
        GameAction action = new GameAction(2, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertTrue(result.isSuccess());
        assertEquals(GameEventType.ACTION_ACCEPTED, result.getEventType());
        assertEquals(3, room.getCurrentPlayerId());
        assertEquals(2, room.getLandlordCandidateId());
    }

    @Test
    void testHandle_PassAction_Success() {
        GameAction action = new GameAction(2, ActionType.PASS, null);
        GameResult result = handler.handle(room, action);

        assertTrue(result.isSuccess());
        assertEquals(GameEventType.ACTION_ACCEPTED, result.getEventType());
        assertEquals(3, room.getCurrentPlayerId());
    }

    @Test
    void testHandle_ReturnToFirstCaller_LandlordDecided() {
        // 设置场景：玩家1是第一个叫地主的，现在轮回到他
        room.setCurrentPlayerId(3);
        room.setFirstCallerId(3);
        room.setLandlordCandidateId(2);

        GameAction action = new GameAction(3, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertTrue(result.isSuccess());
        assertEquals(GameEventType.LANDLORD_DECIDED, result.getEventType());
        assertEquals(GamePhase.PLAYING, room.getCurrentPhase());
        assertEquals(3, room.getLandlordPlayerId());
    }

    @Test
    void testHandle_LandlordDecided_AddsHoleCards() {
        // 设置底牌
        TreeSet<Integer> holeCards = new TreeSet<>(Arrays.asList(50, 51, 52));
        room = new GameRoom(
            Arrays.asList(
                new PlayerState(1, "Player1", new TreeSet<>(Arrays.asList(1, 2, 3))),
                new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(4, 5, 6))),
                new PlayerState(3, "Player3", new TreeSet<>(Arrays.asList(7, 8, 9)))
            ),
            holeCards
        );
        room.setCurrentPhase(GamePhase.ROB_LANDLORD);
        room.setCurrentPlayerId(3);
        room.setFirstCallerId(3);
        room.setLandlordCandidateId(3);

        GameAction action = new GameAction(3, ActionType.CALL, null);
        handler.handle(room, action);

        // 地主应该拿到3张底牌 (初始3张 + 3张底牌 = 6张)
        PlayerState landlord = room.getPlayerById(3);
        assertEquals(6, landlord.getCards().size());
    }

    @Test
    void testHandle_PreviousPasser_ForcedToPass() {
        // 玩家2之前已经PASS过了
        room.addCallPassPlayerId(2);
        room.setCurrentPlayerId(2);

        // 即使尝试叫地主，也会被强制PASS
        GameAction action = new GameAction(2, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertTrue(result.isSuccess());
        // 应该还是PASS，不会成为候选人
        assertEquals(3, room.getCurrentPlayerId());
    }

    @Test
    void testHandle_InvalidActionType() {
        GameAction action = new GameAction(1, null, null);
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
    }

    @Test
    void testHandle_WrongPlayerId() {
        GameAction action = new GameAction(3, ActionType.CALL, null); // 当前是玩家2，玩家3操作应该被拒绝
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
    }

    @Test
    void testHandle_CurrentPlayerIdIsNull() {
        room.setCurrentPlayerId(null);
        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = handler.handle(room, action);

        assertFalse(result.isSuccess());
    }

    @Test
    void testHandle_FullRound_Scenario() {
        // 模拟完整抢地主流程
        // 玩家1是第一个叫地主的，现在轮到玩家2
        room.setCurrentPlayerId(2);
        room.setFirstCallerId(1);

        // 玩家2叫地主
        handler.handle(room, new GameAction(2, ActionType.CALL, null));
        assertEquals(3, room.getCurrentPlayerId());
        assertEquals(2, room.getLandlordCandidateId());

        // 玩家3不抢
        handler.handle(room, new GameAction(3, ActionType.PASS, null));
        assertEquals(1, room.getCurrentPlayerId());

        // 回到玩家1（第一个叫地主的），抢地主结束
        GameResult result = handler.handle(room, new GameAction(1, ActionType.PASS, null));
        assertEquals(GameEventType.LANDLORD_DECIDED, result.getEventType());
        assertEquals(2, room.getLandlordPlayerId());
        assertEquals(GamePhase.PLAYING, room.getCurrentPhase());
    }

    @Test
    void testHandle_ResetsStateAfterLandlordDecided() {
        room.setCurrentPlayerId(3);
        room.setFirstCallerId(3);
        room.setLandlordCandidateId(2);
        room.addCallPassPlayerId(1);
        room.incrementCallPassCount();

        GameAction action = new GameAction(3, ActionType.CALL, null);
        handler.handle(room, action);

        // 验证状态已重置
        assertEquals(0, room.getCallPassCount());
        assertTrue(room.getCallPassPlayerIds().isEmpty());
        assertNull(room.getFirstCallerId());
        assertNull(room.getLandlordCandidateId());
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
