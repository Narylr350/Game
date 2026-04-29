package rule.play;

import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameRoom;
import org.junit.jupiter.api.Test;
import util.CardUtil;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayingRuleCheckerTest {

    @Test
    void should_allow_first_play_without_last_cards() {
        GameRoom room = playingRoom();

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("3"));

        assertEquals(PlayCheckResult.VALID, result);
    }

    @Test
    void should_reject_different_card_type_when_last_cards_exist() {
        GameRoom room = playingRoom();
        room.getPlayingState().setLastPlayedCards(cards("3 3"));

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("4"));

        assertEquals(PlayCheckResult.CARD_TYPE_MISMATCH, result);
    }

    @Test
    void should_reject_same_type_when_main_rank_is_not_bigger() {
        GameRoom room = playingRoom();
        room.getPlayingState().setLastPlayedCards(cards("4"));

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("3"));

        assertEquals(PlayCheckResult.NOT_STRONGER_THAN_LAST, result);
    }

    @Test
    void should_accept_same_type_with_bigger_main_rank() {
        GameRoom room = playingRoom();
        room.getPlayingState().setLastPlayedCards(cards("3"));

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("4"));

        assertEquals(PlayCheckResult.VALID, result);
    }

    @Test
    void should_allow_bomb_to_beat_normal_cards() {
        GameRoom room = playingRoom();
        room.getPlayingState().setLastPlayedCards(cards("A A"));

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("3 3 3 3"));

        assertEquals(PlayCheckResult.VALID, result);
    }

    @Test
    void should_allow_rocket_to_beat_bomb() {
        GameRoom room = playingRoom();
        room.getPlayingState().setLastPlayedCards(cards("3 3 3 3"));

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("小王大王"));

        assertEquals(PlayCheckResult.VALID, result);
    }

    @Test
    void should_reject_bomb_that_is_not_stronger_than_last_bomb() {
        GameRoom room = playingRoom();
        room.getPlayingState().setLastPlayedCards(cards("4 4 4 4"));

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("3 3 3 3"));

        assertEquals(PlayCheckResult.NOT_STRONGER_THAN_LAST, result);
    }

    @Test
    void should_reject_normal_cards_against_rocket() {
        GameRoom room = playingRoom();
        room.getPlayingState().setLastPlayedCards(cards("小王大王"));

        PlayCheckResult result = PlayingRuleChecker.checkPlay(room, cards("3 3 3 3"));

        assertEquals(PlayCheckResult.CARD_TYPE_MISMATCH, result);
    }

    private GameRoom playingRoom() {
        GameFlow gameFlow = new GameFlow();
        GameRoom room = gameFlow.startRoom(List.of("1", "2", "3"));
        room.setCurrentPhase(GamePhase.PLAYING);
        return room;
    }

    private List<Integer> cards(String input) {
        Collection<Integer> handCards = List.of(
                0, 1, 2, 3,
                4, 5, 6, 7,
                8, 9, 10, 11,
                12, 13, 14, 15,
                16, 17, 18, 19,
                20, 21, 22, 23,
                24, 25, 26, 27,
                28, 29, 30, 31,
                32, 33, 34, 35,
                36, 37, 38, 39,
                40, 41, 42, 43,
                44, 45, 46, 47,
                48, 49, 50, 51,
                52, 53
        );
        return CardUtil.stringToCards(input, handCards).stream().toList();
    }
}
