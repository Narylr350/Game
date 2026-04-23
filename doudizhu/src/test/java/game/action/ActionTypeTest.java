package game.action;

import game.enumtype.GamePhase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ActionTypeTest {

    @Test
    void should_treat_blank_input_as_pass_card_in_playing_phase() {
        assertEquals(ActionType.PASS_CARD, ActionType.parseAction("", GamePhase.PLAYING));
    }
}
