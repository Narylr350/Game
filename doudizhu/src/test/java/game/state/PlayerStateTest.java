package game.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlayerState 单元测试类。
 */
class PlayerStateTest {

    private PlayerState playerState;
    private TreeSet<Integer> initialCards;

    @BeforeEach
    void setUp() {
        initialCards = new TreeSet<>(Arrays.asList(1, 5, 9, 13, 53));
        playerState = new PlayerState(1, "TestPlayer", initialCards);
    }

    @Test
    void testConstructor_ValidParameters() {
        PlayerState player = new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(2, 3, 4)));
        
        assertEquals(2, player.getPlayerId());
        assertEquals("Player2", player.getPlayerName());
        assertEquals(3, player.getCards().size());
        assertTrue(player.isOnline());
        assertFalse(player.isLandlord());
    }

    @Test
    void testGetPlayerId() {
        assertEquals(1, playerState.getPlayerId());
    }

    @Test
    void testGetPlayerName() {
        assertEquals("TestPlayer", playerState.getPlayerName());
    }

    @Test
    void testGetCards_ReturnsCopy() {
        TreeSet<Integer> cards = playerState.getCards();
        cards.clear();
        
        assertEquals(5, playerState.getCards().size(), "返回的应该是手牌副本");
    }

    @Test
    void testGetCards_IsSorted() {
        TreeSet<Integer> cards = playerState.getCards();
        Integer[] cardArray = cards.toArray(new Integer[0]);
        
        for (int i = 0; i < cardArray.length - 1; i++) {
            assertTrue(cardArray[i] < cardArray[i + 1], "手牌应该是已排序的");
        }
    }

    @Test
    void testIsLandlord_DefaultFalse() {
        assertFalse(playerState.isLandlord());
    }

    @Test
    void testSetLandlord_True() {
        playerState.setLandlord(true);
        assertTrue(playerState.isLandlord());
    }

    @Test
    void testSetLandlord_False() {
        playerState.setLandlord(true);
        playerState.setLandlord(false);
        assertFalse(playerState.isLandlord());
    }

    @Test
    void testIsOnline_DefaultTrue() {
        assertTrue(playerState.isOnline());
    }

    @Test
    void testSetOnline_False() {
        playerState.setOnline(false);
        assertFalse(playerState.isOnline());
    }

    @Test
    void testSetOnline_True() {
        playerState.setOnline(false);
        playerState.setOnline(true);
        assertTrue(playerState.isOnline());
    }

    @Test
    void testAddCards_EmptySet() {
        TreeSet<Integer> extraCards = new TreeSet<>();
        playerState.addCards(extraCards);
        
        assertEquals(5, playerState.getCards().size());
    }

    @Test
    void testAddCards_WithCards() {
        TreeSet<Integer> extraCards = new TreeSet<>(Arrays.asList(2, 6));
        playerState.addCards(extraCards);
        
        assertEquals(7, playerState.getCards().size());
        assertTrue(playerState.getCards().contains(2));
        assertTrue(playerState.getCards().contains(6));
    }

    @Test
    void testAddCards_DuplicateCards() {
        TreeSet<Integer> extraCards = new TreeSet<>(Arrays.asList(1, 5)); // 已存在的牌
        int originalSize = playerState.getCards().size();
        playerState.addCards(extraCards);
        
        assertEquals(originalSize, playerState.getCards().size(), "重复的牌不应增加手牌数量");
    }

    @Test
    void testAddCards_MaintainsSortOrder() {
        TreeSet<Integer> extraCards = new TreeSet<>(Arrays.asList(54));
        playerState.addCards(extraCards);
        
        TreeSet<Integer> cards = playerState.getCards();
        Integer[] cardArray = cards.toArray(new Integer[0]);
        
        for (int i = 0; i < cardArray.length - 1; i++) {
            assertTrue(cardArray[i] < cardArray[i + 1], "添加牌后应保持排序");
        }
    }

    @Test
    void testImmutability_CardsModification() {
        TreeSet<Integer> cards1 = playerState.getCards();
        TreeSet<Integer> cards2 = playerState.getCards();
        
        // 两次获取的应该是不同的实例
        assertNotSame(cards1, cards2);
    }
}
