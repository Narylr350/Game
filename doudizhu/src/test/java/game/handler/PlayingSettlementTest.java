package game.handler;

import game.action.ActionType;
import game.action.GameAction;
import game.enumtype.GameEventType;
import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.PlayerState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayingSettlementTest {

    @Test
    void should_settle_as_landlord_win_when_landlord_plays_last_card() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        PlayerState landlord = room.getPlayerById(1);
        Integer lastCard = landlord.getCards().first();
        landlord.removeCards(landlord.getCards().stream().filter(card -> !card.equals(lastCard)).toList());

        room.setCurrentPhase(GamePhase.PLAYING);
        room.setLandlordPlayerId(1);
        room.setCurrentPlayerId(1);
        room.getPlayingState().setHighestCardPlayerId(1);

        GameResult result = gameFlow.handlePlayerAction(room, new GameAction(1, ActionType.PLAY_CARD, List.of(lastCard)));

        assertEquals(GameEventType.GAME_SETTLED, result.getEventType());
        assertEquals(GamePhase.SETTLE, room.getCurrentPhase());
        assertEquals(Map.of(
                1, "地主胜利",
                2, "农民失败",
                3, "农民失败"
        ), result.getPlayerMessages());
        assertEquals(1, result.getWinnerPlayerId());
    }

    @Test
    void should_settle_as_farmer_win_when_farmer_plays_last_card() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        PlayerState farmer = room.getPlayerById(2);
        Integer lastCard = farmer.getCards().first();
        farmer.removeCards(farmer.getCards().stream().filter(card -> !card.equals(lastCard)).toList());

        room.setCurrentPhase(GamePhase.PLAYING);
        room.setLandlordPlayerId(1);
        room.setCurrentPlayerId(2);
        room.getPlayingState().setHighestCardPlayerId(2);

        GameResult result = gameFlow.handlePlayerAction(room, new GameAction(2, ActionType.PLAY_CARD, List.of(lastCard)));

        assertEquals(GameEventType.GAME_SETTLED, result.getEventType());
        assertEquals(GamePhase.SETTLE, room.getCurrentPhase());
        assertEquals(Map.of(
                1, "地主失败",
                2, "农民胜利",
                3, "农民胜利"
        ), result.getPlayerMessages());
        assertEquals(2, result.getWinnerPlayerId());
    }
}
