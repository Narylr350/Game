package game;

import org.junit.jupiter.api.Test;

import java.util.List;

public class GameTest {
    @Test
    public void gameTest() {
        GameFlow gameFlow = new GameFlow();
        gameFlow.startRoom(List.of("1", "2", "3"));
        GameRoom currentRoom = gameFlow.getCurrentRoom();
        Integer currentTurnPlayerId = currentRoom.getCurrentTurnPlayerId();
        ActionResult result1 = gameFlow.handlePlayerAction(currentRoom, currentTurnPlayerId, ActionType.CALL);
        ActionResult result2 = gameFlow.handlePlayerAction(currentRoom, currentTurnPlayerId, ActionType.PASS);
        ActionResult result3 = gameFlow.handlePlayerAction(currentRoom, currentTurnPlayerId, ActionType.CALL);
        System.out.println(result1.getNextPlayerId());
        System.out.println(result1.getMessage());
        System.out.println(result2.getNextPlayerId());
        System.out.println(result2.getMessage());
        System.out.println(result3.getNextPlayerId());
        System.out.println(result3.getMessage());
    }
}
