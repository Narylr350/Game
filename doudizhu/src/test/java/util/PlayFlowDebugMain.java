package util;

import game.action.ActionType;
import game.action.GameAction;
import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.PlayerState;
import rule.play.PlayCardGroup;
import rule.play.PlayingRuleChecker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PlayFlowDebugMain {
    public static void main(String[] args) {
        testPlayFlow();
    }

    private static void testPlayFlow() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));

        room.setCurrentPhase(GamePhase.PLAYING);
        room.setLandlordPlayerId(1);
        room.setCurrentPlayerId(1);
        room.getPlayingState().setHighestCardPlayerId(1);

        printRoom(room, "初始状态");
        runInteractiveLoop(gameFlow, room);
    }

    private static void runInteractiveLoop(GameFlow gameFlow, GameRoom room) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            Integer currentPlayerId = room.getCurrentPlayerId();
            PlayerState currentPlayer = room.getPlayerById(currentPlayerId);

            if (currentPlayer == null) {
                System.out.println("当前玩家不存在，结束调试。");
                return;
            }

            System.out.println("==== 当前轮到玩家 " + currentPlayerId + " ====");
            System.out.println("当前阶段 = " + room.getCurrentPhase());
            System.out.println("地主玩家ID = " + room.getLandlordPlayerId());
            System.out.println("当前手牌 = " + CardUtil.cardsToString(currentPlayer.getCards()));
            System.out.println("上一手出的牌 = "
                    + (room.getPlayingState().getLastPlayedCards() == null
                    ? "null"
                    : CardUtil.cardsToString(room.getPlayingState().getLastPlayedCards())));
            System.out.println("请输入动作：直接输入牌面表示出牌，空行或 pass 表示不出，输入 exit 结束：");

            String input = scanner.nextLine().trim();
            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("调试结束。");
                return;
            }

            ActionType actionType = ActionType.parseAction(input, GamePhase.PLAYING);
            if (actionType == null) {
                System.out.println("输入无法识别，请重新输入。");
                System.out.println();
                continue;
            }

            if (actionType == ActionType.PASS_CARD) {
                handlePass(gameFlow, room, currentPlayerId);
                continue;
            }

            handlePlay(gameFlow, room, currentPlayerId, input, currentPlayer);
        }
    }

    private static void handlePlay(GameFlow gameFlow, GameRoom room, int playerId, String input, PlayerState player) {
        try {
            List<Integer> cards = new ArrayList<>(CardUtil.stringToCards(input, player.getCards()));
            System.out.println("==== 玩家" + playerId + " 出牌预览 ====");
            System.out.println(buildPlayPreview(room, input, cards));

            GameAction action = new GameAction(playerId, ActionType.PLAY_CARD, cards);
            GameResult result = gameFlow.handlePlayerAction(room, action);

            System.out.println("==== 玩家" + playerId + " 出牌: " + input + " ====");
            printResult(room, result, playerId);

            if (result.isSuccess()) {
                room.setCurrentPlayerId(room.getNextPlayerId(playerId));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("输入无效 = " + e.getMessage());
            System.out.println();
        }
    }

    static String buildPlayPreview(GameRoom room, String input, List<Integer> cards) {
        StringBuilder builder = new StringBuilder();
        PlayCardGroup currentGroup = PlayCardGroup.analyzeCards(cards);
        List<Integer> lastPlayedCards = room.getPlayingState().getLastPlayedCards();

        builder.append("原始输入 = ").append(input).append(System.lineSeparator());
        builder.append("解析后牌ID = ").append(cards).append(System.lineSeparator());
        builder.append("解析后牌面 = ").append(CardUtil.cardsToString(cards)).append(System.lineSeparator());
        appendGroupInfo(builder, "当前输入", currentGroup);

        if (lastPlayedCards == null || lastPlayedCards.isEmpty()) {
            builder.append("上一手牌面 = null").append(System.lineSeparator());
            builder.append("上一手牌型 = null").append(System.lineSeparator());
            builder.append("上一手主值 = -1").append(System.lineSeparator());
            builder.append("上一手张数 = 0").append(System.lineSeparator());
        } else {
            builder.append("上一手牌ID = ").append(lastPlayedCards).append(System.lineSeparator());
            builder.append("上一手牌面 = ").append(CardUtil.cardsToString(lastPlayedCards)).append(System.lineSeparator());
            appendGroupInfo(builder, "上一手", PlayCardGroup.analyzeCards(lastPlayedCards));
        }

        builder.append("规则检查结果 = ")
                .append(PlayingRuleChecker.checkPlay(room, cards));
        return builder.toString();
    }

    private static void appendGroupInfo(StringBuilder builder, String prefix, PlayCardGroup cardGroup) {
        builder.append(prefix).append("牌型 = ").append(cardGroup.getType()).append(System.lineSeparator());
        builder.append(prefix).append("主值 = ").append(cardGroup.getMainRank()).append(System.lineSeparator());
        builder.append(prefix).append("张数 = ").append(cardGroup.getSize()).append(System.lineSeparator());
    }

    private static void handlePass(GameFlow gameFlow, GameRoom room, int playerId) {
        GameAction action = new GameAction(playerId, ActionType.PASS_CARD, List.of());
        GameResult result = gameFlow.handlePlayerAction(room, action);

        System.out.println("==== 玩家" + playerId + " 不出 ====");
        printResult(room, result, playerId);
    }

    private static void printResult(GameRoom room, GameResult result, int playerId) {
        System.out.println("是否成功 = " + result.isSuccess());
        System.out.println("事件类型 = " + result.getEventType());
        System.out.println("结果消息 = " + result.getMessage());
        System.out.println("当前操作玩家ID = " + room.getCurrentPlayerId());
        System.out.println("玩家" + playerId + " 剩余手牌 = "
                + CardUtil.cardsToString(room.getPlayerById(playerId).getCards()));
        System.out.println("上一手出的牌 = "
                + (room.getPlayingState().getLastPlayedCards() == null
                ? "null"
                : CardUtil.cardsToString(room.getPlayingState().getLastPlayedCards())));
        System.out.println("连续不出次数 = " + room.getPlayingState().getPassCount());
        System.out.println();
    }

    private static void printRoom(GameRoom room, String title) {
        System.out.println("---- " + title + " ----");
        System.out.println("当前阶段 = " + room.getCurrentPhase());
        System.out.println("当前操作玩家ID = " + room.getCurrentPlayerId());
        System.out.println("地主玩家ID = " + room.getLandlordPlayerId());
        for (PlayerState player : room.getPlayers()) {
            System.out.println("玩家" + player.getPlayerId() + " 手牌 = "
                    + CardUtil.cardsToString(player.getCards()));
        }
        System.out.println();
    }
}
