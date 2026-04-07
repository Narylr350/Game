package rule;

import game.GameRoom;
import game.state.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlayRuleChecker 单元测试类。
 */
class PlayRuleCheckerTest {

    private PlayRuleChecker playRuleChecker;
    private GameRoom validRoom;

    @BeforeEach
    void setUp() {
        playRuleChecker = new PlayRuleChecker();
        
        // 创建有效的房间
        List<PlayerState> players = Arrays.asList(
            new PlayerState(1, "Player1", new TreeSet<>(Arrays.asList(1, 2, 3))),
            new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(4, 5, 6))),
            new PlayerState(3, "Player3", new TreeSet<>(Arrays.asList(7, 8, 9)))
        );
        validRoom = new GameRoom(players, new TreeSet<>());
    }

    @Test
    void testCanPlay_ValidRoomAndPlayer() {
        List<Integer> cards = Arrays.asList(1);
        boolean result = playRuleChecker.canPlay(validRoom, 1, cards);
        assertTrue(result, "有效的房间、玩家和牌应返回true");
    }

    @Test
    void testCanPlay_NullRoom() {
        List<Integer> cards = Arrays.asList(1);
        boolean result = playRuleChecker.canPlay(null, 1, cards);
        assertFalse(result, "null房间应返回false");
    }

    @Test
    void testCanPlay_InvalidPlayerId() {
        List<Integer> cards = Arrays.asList(1);
        boolean result = playRuleChecker.canPlay(validRoom, 999, cards);
        assertFalse(result, "不存在的玩家ID应返回false");
    }

    @Test
    void testCanPlay_NullCards() {
        boolean result = playRuleChecker.canPlay(validRoom, 1, null);
        assertFalse(result, "null牌组应返回false");
    }

    @Test
    void testCanPlay_EmptyCards() {
        List<Integer> cards = Collections.emptyList();
        boolean result = playRuleChecker.canPlay(validRoom, 1, cards);
        assertFalse(result, "空牌组应返回false");
    }

    @Test
    void testCanPlay_AllValidPlayers() {
        List<Integer> cards = Arrays.asList(1);
        
        assertTrue(playRuleChecker.canPlay(validRoom, 1, cards), "玩家1应能出牌");
        assertTrue(playRuleChecker.canPlay(validRoom, 2, cards), "玩家2应能出牌");
        assertTrue(playRuleChecker.canPlay(validRoom, 3, cards), "玩家3应能出牌");
    }

    @Test
    void testCanPlay_MultipleCards() {
        List<Integer> cards = Arrays.asList(1, 2, 3);
        boolean result = playRuleChecker.canPlay(validRoom, 1, cards);
        assertTrue(result, "多张牌也应返回true(基础校验不检查牌型)");
    }
}
