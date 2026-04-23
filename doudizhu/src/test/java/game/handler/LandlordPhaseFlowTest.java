package game.handler;

import game.GameEventType;
import game.GameFlow;
import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LandlordPhaseFlowTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void should_cover_landlord_phase_flows(String scenarioName,
                                           List<ActionType> actions,
                                           GameEventType finalEventType,
                                           GamePhase finalPhase,
                                           Integer expectedLandlordId) {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = createRoomWithFirstPlayer(gameFlow);

        GameResult lastResult = null;
        for (int i = 0; i < actions.size(); i++) {
            int currentPlayerId = room.getCurrentPlayerId();
            lastResult = gameFlow.handlePlayerAction(room, new GameAction(currentPlayerId, actions.get(i), null));

            if (i < actions.size() - 1) {
                assertEquals(GameEventType.ACTION_ACCEPTED, lastResult.getEventType(), scenarioName);
            }
        }

        Assertions.assertNotNull(lastResult);
        assertEquals(finalEventType, lastResult.getEventType(), scenarioName);
        assertEquals(finalPhase, room.getCurrentPhase(), scenarioName);

        if (expectedLandlordId == null) {
            assertNull(room.getLandlordPlayerId(), scenarioName);
        } else {
            assertEquals(expectedLandlordId, room.getLandlordPlayerId(), scenarioName);
        }
    }

    private static Stream<Arguments> scenarios() {
        return Stream.of(
                Arguments.of("1不叫 2不叫 3不叫 -> 重新发牌",
                        List.of(ActionType.PASS, ActionType.PASS, ActionType.PASS),
                        GameEventType.REDEAL_REQUIRED,
                        GamePhase.DEALING,
                        null),

                Arguments.of("1不叫 2不叫 3叫 -> 3直接当地主",
                        List.of(ActionType.PASS, ActionType.PASS, ActionType.CALL),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        3),

                Arguments.of("1叫 2不抢 3不抢 -> 1当地主",
                        List.of(ActionType.CALL, ActionType.PASS, ActionType.PASS),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        1),

                Arguments.of("1叫 2抢 3不抢 1不抢 -> 2当地主",
                        List.of(ActionType.CALL, ActionType.CALL, ActionType.PASS, ActionType.PASS),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        2),

                Arguments.of("1叫 2抢 3不抢 1抢 -> 1当地主",
                        List.of(ActionType.CALL, ActionType.CALL, ActionType.PASS, ActionType.CALL),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        1),

                Arguments.of("1叫 2不抢 3抢 1不抢 -> 3当地主",
                        List.of(ActionType.CALL, ActionType.PASS, ActionType.CALL, ActionType.PASS),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        3),

                Arguments.of("1叫 2不抢 3抢 1抢 -> 1当地主",
                        List.of(ActionType.CALL, ActionType.PASS, ActionType.CALL, ActionType.CALL),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        1),

                Arguments.of("1叫 2抢 3抢 1不抢 -> 3当地主",
                        List.of(ActionType.CALL, ActionType.CALL, ActionType.CALL, ActionType.PASS),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        3),

                Arguments.of("1叫 2抢 3抢 1抢 -> 1当地主",
                        List.of(ActionType.CALL, ActionType.CALL, ActionType.CALL, ActionType.CALL),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        1),

                Arguments.of("1不叫 2叫 3不抢 1发CALL也会被强制不抢 -> 2当地主",
                        List.of(ActionType.PASS, ActionType.CALL, ActionType.PASS, ActionType.CALL),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        2),

                Arguments.of("1不叫 2叫 3抢 1发CALL也会被强制不抢 2不抢 -> 3当地主",
                        List.of(ActionType.PASS, ActionType.CALL, ActionType.CALL, ActionType.CALL, ActionType.PASS),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        3),

                Arguments.of("1不叫 2叫 3抢 1发CALL也会被强制不抢 2抢 -> 2当地主",
                        List.of(ActionType.PASS, ActionType.CALL, ActionType.CALL, ActionType.CALL, ActionType.CALL),
                        GameEventType.LANDLORD_DECIDED,
                        GamePhase.PLAYING,
                        2)
        );
    }

    private GameRoom createRoomWithFirstPlayer(GameFlow gameFlow) {
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        room.setCurrentPlayerId(1);
        room.setLandlordPlayerId(null);
        room.getLandlordState().resetLandlordPhaseState();
        return room;
    }
}
