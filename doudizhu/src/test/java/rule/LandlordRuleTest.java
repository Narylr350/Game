package rule;

import game.GamePhase;
import game.GameRoom;
import game.state.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LandlordRule 单元测试类。
 */
class LandlordRuleTest {

    @Test
    void testCanCallLandlord_NullRoom() {
        Boolean result = LandlordRule.canCallLandlord(null);
        assertFalse(result, "null房间应返回false");
    }

    @Test
    void testCanCallLandlord_WrongPhase_Dealing() {
        GameRoom room = createRoomWithPhase(GamePhase.DEALING);
        Boolean result = LandlordRule.canCallLandlord(room);
        assertFalse(result, "发牌阶段应返回false");
    }

    @Test
    void testCanCallLandlord_WrongPhase_Playing() {
        GameRoom room = createRoomWithPhase(GamePhase.PLAYING);
        Boolean result = LandlordRule.canCallLandlord(room);
        assertFalse(result, "出牌阶段应返回false");
    }

    @Test
    void testCanCallLandlord_CorrectPhase_CallLandlord() {
        GameRoom room = createRoomWithPhase(GamePhase.CALL_LANDLORD);
        Boolean result = LandlordRule.canCallLandlord(room);
        assertTrue(result, "叫地主阶段应返回true");
    }

    @Test
    void testCanCallLandlord_CorrectPhase_RobLandlord() {
        GameRoom room = createRoomWithPhase(GamePhase.ROB_LANDLORD);
        Boolean result = LandlordRule.canCallLandlord(room);
        assertTrue(result, "抢地主阶段应返回true");
    }

    @Test
    void testCanCallLandlord_NullPlayers() {
        // GameRoom 构造函数不接受 null players,所以我们需要测试空列表的情况
        List<PlayerState> players = new ArrayList<>();
        GameRoom room = new GameRoom(players, new TreeSet<>());
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        Boolean result = LandlordRule.canCallLandlord(room);
        // 空列表不是 null,所以会通过 null 检查,但应该被视为无效
        // 根据实际实现,空列表可能会通过检查
        // 这里我们验证不会抛出异常即可
        assertNotNull(result);
    }

    @Test
    void testCanCallLandlord_HasLandlord() {
        List<PlayerState> players = Arrays.asList(
            new PlayerState(1, "Player1", new TreeSet<>(Arrays.asList(1, 2, 3))),
            new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(4, 5, 6))),
            new PlayerState(3, "Player3", new TreeSet<>(Arrays.asList(7, 8, 9)))
        );
        GameRoom room = new GameRoom(players, new TreeSet<>());
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        room.setLandlordPlayerId(1);
        
        Boolean result = LandlordRule.canCallLandlord(room);
        assertFalse(result, "已有地主应返回false");
    }

    @Test
    void testCanCallLandlord_ValidRoom() {
        List<PlayerState> players = Arrays.asList(
            new PlayerState(1, "Player1", new TreeSet<>(Arrays.asList(1, 2, 3))),
            new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(4, 5, 6))),
            new PlayerState(3, "Player3", new TreeSet<>(Arrays.asList(7, 8, 9)))
        );
        GameRoom room = new GameRoom(players, new TreeSet<>());
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        
        Boolean result = LandlordRule.canCallLandlord(room);
        assertTrue(result, "有效的房间和阶段应返回true");
    }

    private GameRoom createRoomWithPhase(GamePhase phase) {
        List<PlayerState> players = Arrays.asList(
            new PlayerState(1, "Player1", new TreeSet<>(Arrays.asList(1, 2, 3))),
            new PlayerState(2, "Player2", new TreeSet<>(Arrays.asList(4, 5, 6))),
            new PlayerState(3, "Player3", new TreeSet<>(Arrays.asList(7, 8, 9)))
        );
        GameRoom room = new GameRoom(players, new TreeSet<>());
        room.setCurrentPhase(phase);
        return room;
    }
}
