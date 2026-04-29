package util;

import game.enumtype.GamePhase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GamePromptUtilTest {

    @Test
    void should_format_turn_broadcast_with_player_name_only() throws Exception {
        Method method = GamePromptUtil.class.getDeclaredMethod("turnBroadcast", String.class);
        String message = (String) method.invoke(null, "Narylr");

        assertEquals("系统：当前轮到玩家Narylr操作", message);
    }

    @Test
    void should_format_turn_console_title_with_player_name() throws Exception {
        Method method = GamePromptUtil.class.getDeclaredMethod("turnConsoleTitle", String.class);
        String message = (String) method.invoke(null, "Narylr");

        assertEquals("==== 当前轮到玩家 Narylr ====", message);
    }

    @Test
    void should_prompt_player_to_play_or_press_enter_to_pass() {
        assertEquals("到你出牌了，输入 PASS 或直接回车则过牌", GamePromptUtil.getMessage(GamePhase.PLAYING));
    }

    @Test
    void should_format_played_cards_broadcast_with_player_name() throws Exception {
        Method method = GamePromptUtil.class.getDeclaredMethod("playedCardsBroadcast", String.class, String.class);
        String message = (String) method.invoke(null, "Narylr", "3♣️ 3♥️");

        assertEquals("Narylr出了：\n3♣️ 3♥️", message);
    }

    @Test
    void should_format_pass_broadcast_when_player_skips() throws Exception {
        Method method = GamePromptUtil.class.getDeclaredMethod("playedCardsBroadcast", String.class, String.class);
        String message = (String) method.invoke(null, "Narylr", "");

        assertEquals("Narylr不出", message);
    }

    @Test
    void should_append_remaining_cards_to_played_cards_broadcast() {
        String message = GamePromptUtil.playedCardsBroadcast(
                "Narylr",
                "3♣️ 3♥️",
                "剩余手牌：Narylr 15张，meme 12张，Test 9张"
        );

        assertEquals("Narylr出了：\n3♣️ 3♥️\n剩余手牌：Narylr 15张，meme 12张，Test 9张", message);
    }

    @Test
    void should_append_remaining_cards_to_pass_broadcast() {
        String message = GamePromptUtil.playedCardsBroadcast(
                "Narylr",
                "",
                "剩余手牌：Narylr 15张，meme 12张，Test 9张"
        );

        assertEquals("Narylr不出\n剩余手牌：Narylr 15张，meme 12张，Test 9张", message);
    }

    @Test
    void should_format_remaining_cards_broadcast() {
        String message = GamePromptUtil.remainingCardsBroadcast(List.of("Narylr 15张", "meme 12张", "Test 9张"));

        assertEquals("剩余手牌：Narylr 15张，meme 12张，Test 9张", message);
    }

    @Test
    void should_prompt_all_players_for_next_round_after_settlement() throws Exception {
        Method method = GamePromptUtil.class.getDeclaredMethod("replayPrompt");
        String message = (String) method.invoke(null);

        assertEquals("是否下一把：1.继续 2.退出", message);
    }
}
