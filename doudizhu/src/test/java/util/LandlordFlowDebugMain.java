package util;

import game.GameFlow;
import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.LandlordState;

import java.util.List;

public class LandlordFlowDebugMain {
    public static void main(String[] args) {
        testCallAndRobFlow();
    }

    private static void testCallAndRobFlow() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));

        // 固定起手玩家，避免随机 currentPlayerId 干扰调试
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        room.setCurrentPlayerId(1);
        room.setLandlordPlayerId(null);
        room.getLandlordState().resetLandlordPhaseState();

        printState(room, "初始状态");

        step(gameFlow, room, new GameAction(1, ActionType.CALL, null), "玩家1 叫地主");
        step(gameFlow, room, new GameAction(2, ActionType.CALL, null), "玩家2 抢地主");
        step(gameFlow, room, new GameAction(3, ActionType.PASS, null), "玩家3 不抢");

        // 如果你想继续观察 firstCaller 再表态，可以打开这一句
        // step(gameFlow, room, new GameAction(1, ActionType.PASS, null), "玩家1 不抢");
    }

    private static void step(GameFlow gameFlow, GameRoom room, GameAction action, String title) {
        System.out.println("==== " + title + " ====");
        GameResult result = gameFlow.handlePlayerAction(room, action);
        System.out.println("是否成功 = " + result.isSuccess());
        System.out.println("事件类型 = " + result.getEventType());
        System.out.println("结果消息 = " + result.getMessage());
        printState(room, "处理后");
    }

    private static void printState(GameRoom room, String title) {
        LandlordState state = room.getLandlordState();
        System.out.println("---- " + title + " ----");
        System.out.println("当前阶段 = " + room.getCurrentPhase());
        System.out.println("当前操作玩家ID = " + room.getCurrentPlayerId());
        System.out.println("已确认地主ID = " + room.getLandlordPlayerId());
        System.out.println("首个叫地主玩家ID = " + state.getFirstCallerId());
        System.out.println("当前地主候选人ID = " + state.getLandlordCandidateId());
        System.out.println("不叫次数 = " + state.getCallPassCount());
        System.out.println("叫地主阶段不叫玩家 = " + state.getCallPassPlayerIds());
        System.out.println();    }
}
