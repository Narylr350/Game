package game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameFlowTest {

    @Test
    void startRoomDealsSeventeenCardsToEachPlayerAndThreeHoleCards() {
        GameFlow gameFlow = new GameFlow();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));

        assertEquals(3, room.getPlayers().size());
        assertEquals(3, room.getHoleCards().size());

        Set<Integer> allCards = new TreeSet<>(room.getHoleCards());
        for (PlayerState player : room.getPlayers()) {
            assertEquals(17, player.getCards().size());
            allCards.addAll(player.getCards());
        }

        assertEquals(54, allCards.size());
    }

    @Test
    void startRoomRejectsBlankNames() {
        GameFlow gameFlow = new GameFlow();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> gameFlow.startRoom(List.of("A", " ", "C"))
        );

        assertTrue(error.getMessage().contains("玩家名字不能为空"));
    }

    @Test
    void findPlayerByIdReturnsExistingPlayer() {
        GameFlow gameFlow = new GameFlow();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));

        PlayerState player = room.findPlayerById(2);

        assertNotNull(player);
        assertEquals("B", player.getPlayerName());
        assertFalse(player.getCards().isEmpty());
    }
}
