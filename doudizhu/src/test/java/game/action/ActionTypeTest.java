package game.action;

import game.enumtype.GamePhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ActionTypeTest {

    @Test
    void should_treat_blank_input_as_pass_card_in_playing_phase() {
        assertEquals(ActionType.PASS_CARD, ActionType.parseAction("", GamePhase.PLAYING));
    }

    @Test
    void should_parse_call_landlord_phase_inputs() {
        assertEquals(ActionType.CALL, ActionType.parseAction("1", GamePhase.CALL_LANDLORD));
        assertEquals(ActionType.CALL, ActionType.parseAction("叫地主", GamePhase.CALL_LANDLORD));
        assertEquals(ActionType.PASS, ActionType.parseAction("2", GamePhase.CALL_LANDLORD));
        assertEquals(ActionType.PASS, ActionType.parseAction("不叫", GamePhase.CALL_LANDLORD));
    }

    @Test
    void should_parse_rob_landlord_phase_inputs() {
        assertEquals(ActionType.CALL, ActionType.parseAction("1", GamePhase.ROB_LANDLORD));
        assertEquals(ActionType.CALL, ActionType.parseAction("抢地主", GamePhase.ROB_LANDLORD));
        assertEquals(ActionType.PASS, ActionType.parseAction("2", GamePhase.ROB_LANDLORD));
        assertEquals(ActionType.PASS, ActionType.parseAction("不抢", GamePhase.ROB_LANDLORD));
    }

    @Test
    void should_parse_playing_phase_inputs() {
        assertEquals(ActionType.PASS_CARD, ActionType.parseAction("PASS", GamePhase.PLAYING));
        assertEquals(ActionType.PLAY_CARD, ActionType.parseAction("3 3", GamePhase.PLAYING));
    }

    @Test
    void should_return_null_for_unknown_input_or_missing_phase() {
        assertNull(ActionType.parseAction("抢地主", GamePhase.CALL_LANDLORD));
        assertNull(ActionType.parseAction("1", null));
        assertNull(ActionType.parseAction(null, GamePhase.PLAYING));
    }
}
