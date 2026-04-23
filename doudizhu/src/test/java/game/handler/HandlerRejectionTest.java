package game.handler;

import game.GameEventType;
import game.GameFlow;
import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HandlerRejectionTest {

    @Test
    void should_return_rejected_when_call_handler_receives_wrong_phase_room() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        room.setCurrentPhase(GamePhase.PLAYING);
        room.setCurrentPlayerId(1);

        CallLandlordHandler handler = new CallLandlordHandler(gameFlow);

        GameResult result = assertDoesNotThrow(
                () -> handler.handle(room, new GameAction(1, ActionType.CALL, null))
        );

        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertEquals("当前阶段不能叫地主", result.getMessage());
    }

    @Test
    void should_return_rejected_when_rob_handler_receives_wrong_phase_room() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        room.setCurrentPhase(GamePhase.PLAYING);
        room.setCurrentPlayerId(1);

        RobLandlordHandler handler = new RobLandlordHandler(gameFlow);

        GameResult result = assertDoesNotThrow(
                () -> handler.handle(room, new GameAction(1, ActionType.CALL, null))
        );

        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertEquals("当前阶段不能抢地主", result.getMessage());
    }

    @Test
    void should_return_rejected_when_landlord_has_already_been_decided_in_rob_phase() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        room.setCurrentPhase(GamePhase.ROB_LANDLORD);
        room.setCurrentPlayerId(1);
        room.setLandlordPlayerId(2);

        RobLandlordHandler handler = new RobLandlordHandler(gameFlow);

        GameResult result = assertDoesNotThrow(
                () -> handler.handle(room, new GameAction(1, ActionType.CALL, null))
        );

        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertEquals("地主已确定", result.getMessage());
    }

    @Test
    void should_return_rejected_when_playing_handler_receives_wrong_phase_room() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        room.setCurrentPlayerId(1);

        PlayerState player = room.getPlayerById(1);
        Integer firstCard = player.getCards().first();
        PlayingHandler handler = new PlayingHandler();

        GameResult result = assertDoesNotThrow(
                () -> handler.handle(room, new GameAction(1, ActionType.PLAY_CARD, List.of(firstCard)))
        );

        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
        assertEquals("当前阶段不能出牌", result.getMessage());
    }
}
