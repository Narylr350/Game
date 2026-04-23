package util;

import game.action.ActionType;
import game.action.GameAction;
import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.LandlordState;

import java.util.List;
import java.util.Scanner;

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
        runInteractiveLoop(gameFlow, room);
    }

    private static void runInteractiveLoop(GameFlow gameFlow, GameRoom room) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            GamePhase phase = room.getCurrentPhase();
            if (phase == GamePhase.PLAYING) {
                System.out.println("地主已确认，叫抢地主调试结束。");
                printState(room, "结束状态");
                return;
            }
            if (phase == GamePhase.DEALING) {
                System.out.println("三人都不叫，需要重新发牌，叫地主调试结束。");
                printState(room, "结束状态");
                return;
            }
            if (phase != GamePhase.CALL_LANDLORD && phase != GamePhase.ROB_LANDLORD) {
                System.out.println("当前阶段不是叫抢地主阶段，调试结束。");
                printState(room, "结束状态");
                return;
            }

            Integer currentPlayerId = room.getCurrentPlayerId();
            System.out.println("==== 当前轮到玩家 " + currentPlayerId + " ====");
            printInputTip(phase);

            String input = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("调试结束。");
                return;
            }

            ActionType actionType = ActionType.parseAction(input, phase);
            if (actionType == null) {
                System.out.println("输入无法识别，请重新输入。");
                System.out.println();
                continue;
            }

            step(gameFlow, room, new GameAction(currentPlayerId, actionType, null), "玩家" + currentPlayerId + " 输入 " + input);
        }
    }

    private static void printInputTip(GamePhase phase) {
        if (phase == GamePhase.CALL_LANDLORD) {
            System.out.println("请输入动作：1/叫/叫地主 表示叫地主，2/不叫 表示不叫，exit 结束：");
            return;
        }
        System.out.println("请输入动作：1/抢/抢地主 表示抢地主，2/不抢 表示不抢，exit 结束：");
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
