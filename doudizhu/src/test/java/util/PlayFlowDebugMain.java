package util;

import game.GameFlow;
import game.GamePhase;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;
import game.state.PlayingState;

import java.util.ArrayList;
import java.util.List;

public class PlayFlowDebugMain {
    public static void main(String[] args) {
        testPlayFlow();
    }

    private static void testPlayFlow() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));

        // 直接进入出牌阶段，避免叫/抢地主随机流程影响调试
        room.setCurrentPhase(GamePhase.PLAYING);
        room.setLandlordPlayerId(1);
        room.setCurrentPlayerId(1);
        room.getPlayingState().setHighestCardPlayerId(1);

        PlayerState player1 = room.getPlayerById(1);
        PlayerState player2 = room.getPlayerById(2);
        PlayingState playingState = room.getPlayingState();

        printRoom(room, "初始状态");

        play(gameFlow, room, 1, "10 J Q K A", player1);

        // 当前 PlayingHandler 出牌后不会自动切到下家，这里手动切换方便继续调试
        room.setCurrentPlayerId(2);

        play(gameFlow, room, 2, "J Q K A 2", player2);
        pass(gameFlow, room, 2);
        pass(gameFlow, room, 3);

        room.setCurrentPlayerId(1);
        play(gameFlow, room, 1, "3 3 3 4", player1);

        System.out.println("最后上一手 = "
                + (playingState.getLastPlayedCards() == null
                ? "null"
                : CardUtil.cardsToString(playingState.getLastPlayedCards())));
        printRoom(room, "结束状态");
    }

    private static void play(GameFlow gameFlow, GameRoom room, int playerId, String input, PlayerState player) {
        List<Integer> cards = new ArrayList<>(CardUtil.stringToCards(input, player.getCards()));
        GameAction action = new GameAction(playerId, ActionType.PLAY_CARD, cards);
        GameResult result = gameFlow.handlePlayerAction(room, action);

        System.out.println("==== 玩家" + playerId + " 出牌: " + input + " ====");
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
        System.out.println();
    }

    private static void pass(GameFlow gameFlow, GameRoom room, int playerId) {
        GameAction action = new GameAction(playerId, ActionType.PASS_CARD, List.of());
        GameResult result = gameFlow.handlePlayerAction(room, action);

        System.out.println("==== 玩家" + playerId + " 不出 ====");
        System.out.println("是否成功 = " + result.isSuccess());
        System.out.println("事件类型 = " + result.getEventType());
        System.out.println("结果消息 = " + result.getMessage());
        System.out.println("当前操作玩家ID = " + room.getCurrentPlayerId());
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
