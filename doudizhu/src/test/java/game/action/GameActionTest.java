package game.action;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameActionTest {

    @Test
    void should_copy_cards_to_protect_action_from_external_list_changes() {
        List<Integer> cards = new ArrayList<>(List.of(1, 2, 3));

        GameAction action = new GameAction(1, ActionType.PLAY_CARD, cards);
        cards.add(4);

        assertEquals(List.of(1, 2, 3), action.getCards());
    }

    @Test
    void should_return_immutable_cards() {
        GameAction action = new GameAction(1, ActionType.PLAY_CARD, List.of(1, 2, 3));

        assertThrows(UnsupportedOperationException.class, () -> action.getCards().add(4));
    }

    @Test
    void should_treat_null_cards_as_empty_list() {
        GameAction action = new GameAction(1, ActionType.PASS, null);

        assertTrue(action.getCards().isEmpty());
    }
}
