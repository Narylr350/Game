package game;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        assertThrows(
                IllegalArgumentException.class,
                () -> gameFlow.startRoom(List.of("A", " ", "C"))
        );
    }

    @Test
    void startRoomRejectsFullWidthSpaceNames() {
        GameFlow gameFlow = new GameFlow();

        assertThrows(
                IllegalArgumentException.class,
                () -> gameFlow.startRoom(List.of("A", "\u3000", "C"))
        );
    }

    @Test
    void startRoomInitializesLifecycleState() {
        GameFlow gameFlow = new GameFlow();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));

        assertEquals(true, room.isGameStarted());
        assertEquals(false, room.isGameFinished());
        assertEquals(null, room.getLandlordPlayerId());
        assertEquals(null, room.getCurrentTurnPlayerId());
    }

    @Test
    void roomCanTrackTurnLandlordAndReceiveHoleCards() {
        GameFlow gameFlow = new GameFlow();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));
        PlayerState landlord = room.getPlayers().get(0);

        room.setCurrentTurnPlayerId(landlord.getPlayerId());
        room.setLandlord(landlord.getPlayerId());
        landlord.addCards(room.getHoleCards());

        assertEquals(landlord.getPlayerId(), room.getCurrentTurnPlayerId());
        assertEquals(landlord.getPlayerId(), room.getLandlordPlayerId());
        assertEquals(true, landlord.isLandlord());
        assertEquals(20, landlord.getCards().size());
        assertEquals(true, landlord.getCards().containsAll(room.getHoleCards()));
    }

    @Test
    void setLandlordNullClearsLandlordStateWithoutThrowing() {
        GameFlow gameFlow = new GameFlow();

        GameRoom room = gameFlow.startRoom(List.of("A", "B", "C"));
        room.setLandlord(room.getPlayers().get(0).getPlayerId());

        room.setLandlord(null);

        assertEquals(null, room.getLandlordPlayerId());
        for (PlayerState player : room.getPlayers()) {
            assertFalse(player.isLandlord());
        }
    }

    @Test
    void findPlayerByIdReturnsExistingPlayer() {
        GameFlow gameFlow = new GameFlow();

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
