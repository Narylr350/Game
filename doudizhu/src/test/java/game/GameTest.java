package game;

import org.junit.jupiter.api.Test;

import java.util.List;

public class GameTest {
    @Test
    public void playerWhoPassedInCallStageCannotRobLater() {
        GameFlow gameFlow = GameFlow.getInstance();
        gameFlow.startRoom(List.of("1", "2", "3"));
        GameRoom room = gameFlow.getCurrentRoom();

        // 1 号玩家：不叫
        Integer player1 = room.getCurrentTurnPlayerId();
        GameActionResult r1 = gameFlow.handlePlayerAction(room, player1, ActionType.PASS);


        // 2 号玩家：叫地主
        Integer player2 = room.getCurrentTurnPlayerId();
        GameActionResult r2 = gameFlow.handlePlayerAction(room, player2, ActionType.CALL);

        // 3 号玩家：抢地主
        Integer player3 = room.getCurrentTurnPlayerId();
        GameActionResult r3 = gameFlow.handlePlayerAction(room, player3, ActionType.CALL);

        // 1 号
        Integer player1Again = room.getCurrentTurnPlayerId();
        GameActionResult r4 = gameFlow.handlePlayerAction(room, player1Again, ActionType.PASS);

        // 现在是2号
        Integer player2Again = room.getCurrentTurnPlayerId();
        GameActionResult r5 = gameFlow.handlePlayerAction(room, player2Again, ActionType.PASS);

        System.out.println("r1: " + r1.getDisplayMessage());
        System.out.println("r2: " + r2.getDisplayMessage());
        System.out.println("r3: " + r3.getDisplayMessage());
        System.out.println("r4: " + r4.getDisplayMessage());
        System.out.println("r5: " + r5.getDisplayMessage());

        // 你先至少观察这几个点
        System.out.println("当前阶段: " + room.getPhase());
        System.out.println("当前轮到谁: " + room.getCurrentTurnPlayerId());
        System.out.println("当前候选地主: " + room.getLandLordCandidateId());
        System.out.println("当前地主：" + room.getLandLordPlayerId());
    }
}
