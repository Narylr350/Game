package game;

import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameFlow 单元测试类。
 */
class GameFlowTest {

    private GameFlow gameFlow;

    @BeforeEach
    void setUp() {
        gameFlow = new GameFlow();
    }

    @Test
    void testDeal_ValidPlayerNames() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        DealResult result = gameFlow.deal(playerNames);

        assertNotNull(result);
        assertEquals(3, result.getPlayers().size());
        assertEquals(3, result.getHoleCards().size());
    }

    @Test
    void testDeal_EachPlayerHas17Cards() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        DealResult result = gameFlow.deal(playerNames);

        for (PlayerState player : result.getPlayers()) {
            assertEquals(17, player.getCards().size(), "每个玩家应该有17张牌");
        }
    }

    @Test
    void testDeal_AllCardsDistributed() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        DealResult result = gameFlow.deal(playerNames);

        int totalCards = result.getHoleCards().size();
        for (PlayerState player : result.getPlayers()) {
            totalCards += player.getCards().size();
        }

        assertEquals(54, totalCards, "总牌数应该是54张");
    }

    @Test
    void testDeal_NoDuplicateCards() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        DealResult result = gameFlow.deal(playerNames);

        TreeSet<Integer> allCards = new TreeSet<>(result.getHoleCards());
        for (PlayerState player : result.getPlayers()) {
            for (Integer card : player.getCards()) {
                assertFalse(allCards.contains(card), "不应该有重复的牌: " + card);
                allCards.add(card);
            }
        }

        assertEquals(54, allCards.size(), "所有牌应该不重复");
    }

    @Test
    void testDeal_InvalidPlayerNames() {
        assertThrows(IllegalArgumentException.class, () ->
            gameFlow.deal(Arrays.asList("张三", "李四"))
        );
    }

    @Test
    void testDeal_NullPlayerNames() {
        assertThrows(IllegalArgumentException.class, () ->
            gameFlow.deal(null)
        );
    }

    @Test
    void testStartRoom_ValidNames() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        GameRoom room = gameFlow.startRoom(playerNames);

        assertNotNull(room);
        assertEquals(GamePhase.CALL_LANDLORD, room.getCurrentPhase());
        assertNull(room.getLandlordPlayerId());
        assertNotNull(room.getCurrentPlayerId());
    }

    @Test
    void testStartCallLandLord_SetsCorrectPhase() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        DealResult dealResult = gameFlow.deal(playerNames);
        GameRoom room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());

        gameFlow.startCallLandLord(room);

        assertEquals(GamePhase.CALL_LANDLORD, room.getCurrentPhase());
        assertNull(room.getLandlordPlayerId());
        assertTrue(room.getCurrentPlayerId() >= 1 && room.getCurrentPlayerId() <= 3);
    }

    @Test
    void testReDeal_CreatesNewRoom() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        GameRoom oldRoom = gameFlow.startRoom(playerNames);

        GameRoom newRoom = gameFlow.reDeal(oldRoom);

        assertNotNull(newRoom);
        assertNotSame(oldRoom, newRoom);
        assertEquals(GamePhase.CALL_LANDLORD, newRoom.getCurrentPhase());
        assertEquals(3, newRoom.getPlayers().size());
    }

    @Test
    void testHandlePlayerAction_DealingPhase() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        DealResult dealResult = gameFlow.deal(playerNames);
        GameRoom room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        room.setCurrentPhase(GamePhase.DEALING);

        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = gameFlow.handlePlayerAction(room, action);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(GameEventType.REDEAL_REQUIRED, result.getEventType());
    }

    @Test
    void testHandlePlayerAction_PlayingPhase() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        GameRoom room = gameFlow.startRoom(playerNames);
        room.setCurrentPhase(GamePhase.PLAYING);

        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = gameFlow.handlePlayerAction(room, action);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals(GameEventType.ACTION_REJECTED, result.getEventType());
    }

    @Test
    void testHandlePlayerAction_WaitingPhase() {
        List<String> playerNames = Arrays.asList("张三", "李四", "王五");
        GameRoom room = gameFlow.startRoom(playerNames);
        room.setCurrentPhase(GamePhase.WAITING);

        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameResult result = gameFlow.handlePlayerAction(room, action);

        assertNotNull(result);
        assertFalse(result.isSuccess());
    }
}
