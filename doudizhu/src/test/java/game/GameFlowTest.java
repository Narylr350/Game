package game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameFlowTest {

    @Test
    void startRoomDealsSeventeenCardsToEachPlayerAndThreeHoleCards() {
        GameFlow gameFlow = GameFlow.getInstance();

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
        GameFlow gameFlow = GameFlow.getInstance();

        assertThrows(
                IllegalArgumentException.class,
                () -> gameFlow.startRoom(List.of("A", " ", "C"))
        );
    }

    @Test
    void startRoomRejectsFullWidthSpaceNames() {
        GameFlow gameFlow = GameFlow.getInstance();

        assertThrows(
                IllegalArgumentException.class,
                () -> gameFlow.startRoom(List.of("A", "\u3000", "C"))
        );
    }

    @Test
    void startRoomInitializesLifecycleState() {
        GameFlow gameFlow = GameFlow.getInstance();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));

        assertNull(room.getLandLordPlayerId());
        assertNull(room.getCurrentTurnPlayerId());
    }

    @Test
    void roomCanTrackTurnLandlordAndReceiveHoleCards() {
        GameFlow gameFlow = GameFlow.getInstance();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));
        PlayerState landlord = room.getPlayers().get(0);

        room.setCurrentTurnPlayerId(landlord.getPlayerId());
        room.setLandLordId(landlord.getPlayerId());
        landlord.addCards(room.getHoleCards());

        assertEquals(landlord.getPlayerId(), room.getCurrentTurnPlayerId());
        assertEquals(landlord.getPlayerId(), room.getLandLordPlayerId());
        assertTrue(landlord.isLandlord());
        assertEquals(20, landlord.getCards().size());
        assertTrue(landlord.getCards()
                .containsAll(room.getHoleCards()));
    }

    @Test
    void setLandlordNullClearsLandlordStateWithoutThrowing() {
        GameFlow gameFlow = GameFlow.getInstance();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));
        room.setLandLordId(room.getPlayers().get(0).getPlayerId());

        room.setLandLordId(null);

        assertNull(room.getLandLordPlayerId());
        for (PlayerState player : room.getPlayers()) {
            assertFalse(player.isLandlord());
        }
    }

    @Test
    void findPlayerByIdReturnsExistingPlayer() {
        GameFlow gameFlow = GameFlow.getInstance();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));

        PlayerState expectedPlayer = room.getPlayers().stream()
                .filter(player -> "B".equals(player.getPlayerName()))
                .findFirst()
                .orElseThrow();
        PlayerState player = room.findPlayerById(expectedPlayer.getPlayerId());

        assertNotNull(player);
        assertEquals(expectedPlayer.getPlayerName(), player.getPlayerName());
        assertFalse(player.getCards().isEmpty());
    }
}
