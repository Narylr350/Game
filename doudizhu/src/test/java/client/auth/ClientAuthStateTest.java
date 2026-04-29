package client.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientAuthStateTest {

    @Test
    void should_mark_authenticated_after_login_success_message() {
        ClientAuthState state = new ClientAuthState();

        state.consumeServerMessage("登录成功,游戏启动~");

        assertTrue(state.isAuthenticated());
    }

    @Test
    void should_ignore_regular_game_messages() {
        ClientAuthState state = new ClientAuthState();

        state.consumeServerMessage("系统：当前轮到玩家alice操作");

        assertFalse(state.isAuthenticated());
    }
}
