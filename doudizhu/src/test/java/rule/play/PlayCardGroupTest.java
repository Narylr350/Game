package rule.play;

import org.junit.jupiter.api.Test;
import util.CardUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayCardGroupTest {

    private List<Integer> testHandCards() {
        return List.of(
                0, 1, 2, 3,      // 3
                4, 5, 6, 7,      // 4
                8, 9, 10, 11,    // 5
                12, 13, 14, 15,  // 6
                16, 17, 18, 19,  // 7
                20, 21, 22, 23,  // 8
                24, 25, 26, 27,  // 9
                28, 29, 30, 31,  // 10
                32, 33, 34, 35,  // J
                36, 37, 38, 39,  // Q
                40, 41, 42, 43,  // K
                44, 45, 46, 47,  // A
                48, 49, 50, 51,  // 2
                52, 53           // 小王 大王
        );
    }

    private PlayCardGroup parseFromInput(String input) {
        return PlayCardGroup.analyzeCards(
                CardUtil.stringToCards(input, testHandCards())
        );
    }

    private void assertMainRank(String input, int expectedRank) {
        assertEquals(expectedRank, parseFromInput(input).getMainRank());
    }

    @Test
    void should_parse_single() {
        assertEquals(CardType.SINGLE, parseFromInput("3").getType());
    }

    @Test
    void should_parse_pair() {
        assertEquals(CardType.PAIR, parseFromInput("3 3").getType());
    }

    @Test
    void should_parse_triple() {
        assertEquals(CardType.TRIPLE, parseFromInput("3 3 3").getType());
    }

    @Test
    void should_parse_bomb() {
        assertEquals(CardType.BOMB, parseFromInput("3 3 3 3").getType());
    }

    @Test
    void should_parse_rocket() {
        assertEquals(CardType.ROCKET, parseFromInput("小王大王").getType());
    }

    @Test
    void should_return_invalid_for_fake_rocket() {
        assertEquals(CardType.INVALID, parseFromInput("3 小王 大王").getType());
    }

    @Test
    void should_parse_straight() {
        assertEquals(CardType.STRAIGHT, parseFromInput("3 4 5 6 7").getType());
    }

    @Test
    void should_parse_long_straight() {
        assertEquals(CardType.STRAIGHT, parseFromInput("3 4 5 6 7 8 9 10 J Q").getType());
    }

    @Test
    void should_return_invalid_for_broken_straight() {
        assertEquals(CardType.INVALID, parseFromInput("3 4 5 6 8").getType());
    }

    @Test
    void should_return_invalid_for_straight_with_duplicate_rank() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 4 5 6").getType());
    }

    @Test
    void should_return_invalid_for_straight_with_two() {
        assertEquals(CardType.INVALID, parseFromInput("10 J Q K A 2").getType());
    }

    @Test
    void should_return_invalid_for_straight_with_joker() {
        assertEquals(CardType.INVALID, parseFromInput("10 J Q K 小王").getType());
    }

    @Test
    void should_parse_consecutive_pairs() {
        assertEquals(CardType.CONSECUTIVE_PAIRS, parseFromInput("3 3 4 4 5 5").getType());
    }

    @Test
    void should_parse_long_consecutive_pairs() {
        assertEquals(CardType.CONSECUTIVE_PAIRS, parseFromInput("3 3 4 4 5 5 6 6").getType());
    }

    @Test
    void should_return_invalid_for_non_consecutive_pairs() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 4 4 6 6").getType());
    }

    @Test
    void should_return_invalid_for_fake_consecutive_pairs() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 4 4 5 6").getType());
    }

    @Test
    void should_return_invalid_for_consecutive_pairs_with_two() {
        assertEquals(CardType.INVALID, parseFromInput("J J Q Q K K A A 2 2").getType());
    }

    @Test
    void should_return_invalid_for_consecutive_pairs_with_joker() {
        assertEquals(CardType.INVALID, parseFromInput("Q Q K K 小王 大王").getType());
    }

    // ===== 下面这些是你后面要补的常见牌型 =====

    @Test
    void should_parse_three_with_one() {
        assertEquals(CardType.THREE_WITH_ONE, parseFromInput("3 3 3 4").getType());
    }

    @Test
    void should_parse_three_with_one_regardless_of_order() {
        assertEquals(CardType.THREE_WITH_ONE, parseFromInput("4 3 3 3").getType());
    }

    @Test
    void should_use_triple_rank_as_main_rank_for_three_with_one() {
        assertMainRank("3 3 3 小王", 1);
    }

    @Test
    void should_parse_three_with_pair() {
        assertEquals(CardType.THREE_WITH_PAIR, parseFromInput("3 3 3 4 4").getType());
    }

    @Test
    void should_parse_three_with_pair_regardless_of_order() {
        assertEquals(CardType.THREE_WITH_PAIR, parseFromInput("4 4 3 3 3").getType());
    }

    @Test
    void should_return_invalid_for_four_cards_that_are_not_three_with_one() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 4 4").getType());
    }

    @Test
    void should_return_invalid_for_five_cards_that_are_not_three_with_pair() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 4 4 5").getType());
    }

    @Test
    void should_parse_airplane() {
        assertEquals(CardType.AIRPLANE, parseFromInput("3 3 3 4 4 4").getType());
    }

    @Test
    void should_parse_long_airplane() {
        assertEquals(CardType.AIRPLANE, parseFromInput("3 3 3 4 4 4 5 5 5").getType());
    }

    @Test
    void should_return_invalid_for_broken_airplane() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 3 5 5 5").getType());
    }

    @Test
    void should_return_invalid_for_airplane_with_two() {
        assertEquals(CardType.INVALID, parseFromInput("Q Q Q K K K A A A 2 2 2").getType());
    }

    @Test
    void should_parse_airplane_with_single_wings() {
        assertEquals(CardType.AIRPLANE_WITH_SINGLE_WINGS, parseFromInput("3 3 3 4 4 4 5 6").getType());
    }

    @Test
    void should_use_airplane_body_rank_as_main_rank_for_single_wings() {
        assertMainRank("3 3 3 4 4 4 小王 大王", 2);
    }

    @Test
    void should_parse_airplane_with_pair_wings() {
        assertEquals(CardType.AIRPLANE_WITH_PAIR_WINGS, parseFromInput("3 3 3 4 4 4 5 5 6 6").getType());
    }

    @Test
    void should_use_airplane_body_rank_as_main_rank_for_pair_wings() {
        assertMainRank("3 3 3 4 4 4 A A 2 2", 2);
    }

    @Test
    void should_return_invalid_for_airplane_with_wrong_single_wings_count() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 3 4 4 4 5").getType());
    }

    @Test
    void should_return_invalid_for_airplane_with_wrong_pair_wings_count() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 3 4 4 4 5 5 6").getType());
    }

    @Test
    void should_return_invalid_for_airplane_with_non_pair_wings() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 3 4 4 4 5 5 6 7").getType());
    }

    @Test
    void should_parse_four_with_two_single() {
        assertEquals(CardType.FOUR_WITH_TWO_SINGLE, parseFromInput("3 3 3 3 4 5").getType());
    }

    @Test
    void should_parse_four_with_two_pair() {
        assertEquals(CardType.FOUR_WITH_TWO_PAIR, parseFromInput("3 3 3 3 4 4 5 5").getType());
    }

    @Test
    void should_return_invalid_for_fake_four_with_two_single() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 3 4 5 6").getType());
    }

    @Test
    void should_return_invalid_for_fake_four_with_two_pair() {
        assertEquals(CardType.INVALID, parseFromInput("3 3 3 3 4 4 5 6").getType());
    }
}
