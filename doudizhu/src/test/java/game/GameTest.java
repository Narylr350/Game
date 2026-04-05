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
        ActionResult result1 = gameFlow.handlePlayerAction(currentRoom, currentTurnPlayerId, ActionType.PASS);

        currentTurnPlayerId = currentRoom.getCurrentTurnPlayerId();
        ActionResult result2 = gameFlow.handlePlayerAction(currentRoom, currentTurnPlayerId, ActionType.CALL);

        currentTurnPlayerId = currentRoom.getCurrentTurnPlayerId();
        ActionResult result3 = gameFlow.handlePlayerAction(currentRoom, currentTurnPlayerId, ActionType.CALL);

        System.out.println("当前id" + result1.getTargetPlayerId());
        System.out.println("下一个id" + result1.getNextPlayerId());

        System.out.println("当前id" + result2.getTargetPlayerId());
        System.out.println("下一个id" + result2.getNextPlayerId());

        System.out.println("地主id" + result3.getLandlordId());
        System.out.println(result3.getMessage());
    }
}
