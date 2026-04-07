package game;

import game.state.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameRoom 单元测试类。
 */
class GameRoomTest {

    private GameRoom room;
    private List<PlayerState> players;
    private TreeSet<Integer> holeCards;

    @BeforeEach
    void setUp() {
        players = Arrays.asList(
            new PlayerState(1, "Player1", new TreeSet<>(Arrays.asList(1, 2, 3))),
            new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(4, 5, 6))),
            new PlayerState(3, "Player3", new TreeSet<>(Arrays.asList(7, 8, 9)))
        );
        holeCards = new TreeSet<>(Arrays.asList(10, 11, 12));
        room = new GameRoom(players, holeCards);
    }

    @Test
    void testConstructor_ValidParameters() {
        assertNotNull(room);
        assertEquals(3, room.getPlayers().size());
        assertEquals(3, room.getHoleCards().size());
    }

    @Test
    void testGetCurrentPhase_DefaultNull() {
        assertNull(room.getCurrentPhase());
    }

    @Test
    void testSetCurrentPhase() {
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        assertEquals(GamePhase.CALL_LANDLORD, room.getCurrentPhase());
    }

    @Test
    void testGetPlayers_ReturnsUnmodifiableList() {
        List<PlayerState> playerList = room.getPlayers();
        assertThrows(UnsupportedOperationException.class, () ->
            playerList.add(new PlayerState(4, "Player4", new TreeSet<>()))
        );
    }

    @Test
    void testGetPlayerById_ExistingPlayer() {
        PlayerState player = room.getPlayerById(1);
        assertNotNull(player);
        assertEquals("Player1", player.getPlayerName());
    }

    @Test
    void testGetPlayerById_NonExistingPlayer() {
        PlayerState player = room.getPlayerById(999);
        assertNull(player);
    }

    @Test
    void testGetCurrentPlayerId_DefaultNull() {
        assertNull(room.getCurrentPlayerId());
    }

    @Test
    void testSetCurrentPlayerId() {
        room.setCurrentPlayerId(2);
        assertEquals(2, room.getCurrentPlayerId());
    }

    @Test
    void testGetLandlordPlayerId_DefaultNull() {
        assertNull(room.getLandlordPlayerId());
    }

    @Test
    void testSetLandlordPlayerId_SetsLandlord() {
        room.setLandlordPlayerId(1);
        assertEquals(1, room.getLandlordPlayerId());
        assertTrue(room.getPlayerById(1).isLandlord());
        assertFalse(room.getPlayerById(2).isLandlord());
        assertFalse(room.getPlayerById(3).isLandlord());
    }

    @Test
    void testSetLandlordPlayerId_NullClearsLandlord() {
        room.setLandlordPlayerId(1);
        room.setLandlordPlayerId(null);
        assertNull(room.getLandlordPlayerId());
        assertFalse(room.getPlayerById(1).isLandlord());
    }

    @Test
    void testGetCallPassCount_DefaultZero() {
        assertEquals(0, room.getCallPassCount());
    }

    @Test
    void testIncrementCallPassCount() {
        room.incrementCallPassCount();
        room.incrementCallPassCount();
        assertEquals(2, room.getCallPassCount());
    }

    @Test
    void testResetCallPassCount() {
        room.incrementCallPassCount();
        room.incrementCallPassCount();
        room.resetCallPassCount();
        assertEquals(0, room.getCallPassCount());
    }

    @Test
    void testGetCallPassPlayerIds_DefaultEmpty() {
        assertTrue(room.getCallPassPlayerIds().isEmpty());
    }

    @Test
    void testAddCallPassPlayerId() {
        room.addCallPassPlayerId(1);
        room.addCallPassPlayerId(2);
        assertEquals(2, room.getCallPassPlayerIds().size());
        assertTrue(room.getCallPassPlayerIds().contains(1));
        assertTrue(room.getCallPassPlayerIds().contains(2));
    }

    @Test
    void testResetCallPasserId() {
        room.addCallPassPlayerId(1);
        room.addCallPassPlayerId(2);
        room.resetCallPasserId();
        assertTrue(room.getCallPassPlayerIds().isEmpty());
    }

    @Test
    void testGetFirstCallerId_DefaultNull() {
        assertNull(room.getFirstCallerId());
    }

    @Test
    void testSetFirstCallerId() {
        room.setFirstCallerId(2);
        assertEquals(2, room.getFirstCallerId());
    }

    @Test
    void testResetFirstCallerId() {
        room.setFirstCallerId(2);
        room.resetFirstCallerId();
        assertNull(room.getFirstCallerId());
    }

    @Test
    void testGetLandlordCandidateId_DefaultNull() {
        assertNull(room.getLandlordCandidateId());
    }

    @Test
    void testSetLandlordCandidateId() {
        room.setLandlordCandidateId(3);
        assertEquals(3, room.getLandlordCandidateId());
    }

    @Test
    void testResetLandlordCandidateId() {
        room.setLandlordCandidateId(3);
        room.resetLandlordCandidateId();
        assertNull(room.getLandlordCandidateId());
    }

    @Test
    void testGetHoleCards_ReturnsCopy() {
        TreeSet<Integer> holeCards1 = room.getHoleCards();
        TreeSet<Integer> holeCards2 = room.getHoleCards();
        assertNotSame(holeCards1, holeCards2);
    }

    @Test
    void testGetNextPlayerId_NormalCase() {
        room.setCurrentPlayerId(1);
        Integer nextId = room.getNextPlayerId(1);
        assertEquals(2, nextId);
    }

    @Test
    void testGetNextPlayerId_WrapsAround() {
        Integer nextId = room.getNextPlayerId(3);
        assertEquals(1, nextId);
    }

    @Test
    void testGetNextPlayerId_NullInput() {
        assertNull(room.getNextPlayerId(null));
    }

    @Test
    void testGetNextPlayerId_InvalidPlayerId() {
        assertNull(room.getNextPlayerId(999));
    }

    @Test
    void testGetPlayState_NotNull() {
        assertNotNull(room.getPlayState());
    }
}
