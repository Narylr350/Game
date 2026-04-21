package util;

import game.GameFlow;
import game.GameResult;
import game.GameRoom;
import game.action.ActionType;
import game.action.GameAction;
import game.state.PlayerState;
import game.state.PlayingState;

import java.util.List;

public class UtilTest {
    public static void main(String[] args) {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        GameAction action = new GameAction(1, ActionType.CALL, null);
        GameAction action1 = new GameAction(2, ActionType.PASS, null);
        GameAction action2 = new GameAction(3, ActionType.PASS, null);
        gameFlow.handlePlayerAction(room, action);
        gameFlow.handlePlayerAction(room, action1);
        gameFlow.handlePlayerAction(room, action2);
        gameFlow.handlePlayerAction(room, action);
        System.out.println(room.getLandlordPlayerId());
        PlayerState player1 = room.getPlayerById(1);
        PlayerState player2 = room.getPlayerById(2);
        PlayerState player3 = room.getPlayerById(3);
        System.out.println(CardUtil.cardsToString(player1.getCards()));
        System.out.println(CardUtil.cardsToString(player2.getCards()));
        System.out.println(CardUtil.cardsToString(player3.getCards()));
        GameAction action3 = new GameAction(1, ActionType.PLAY_CARD, (List<Integer>) CardUtil.stringToCards("10jqk", player1.getCards()));
        GameResult gameResult = gameFlow.handlePlayerAction(room, action3);
        PlayingState playingState = room.getPlayingState();
        System.out.println(gameResult.getMessage());
        System.out.println(CardUtil.cardsToString(playingState.getLastPlayedCards()));
        System.out.println(CardUtil.cardsToString(player1.getCards()));

    }
}
