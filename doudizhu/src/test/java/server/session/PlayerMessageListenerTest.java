package server.session;

import org.junit.jupiter.api.Test;
import server.flow.TurnInputCoordinator;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerMessageListenerTest {

    @Test
    void should_close_player_session_when_exit_is_received_after_authentication() {
        PlayerSessionRegistry registry = new PlayerSessionRegistry();
        StringWriter output = new StringWriter();
        PlayerSession session = new PlayerSession(
                1,
                "alice",
                new Socket(),
                new BufferedReader(new StringReader("exit\n")),
                new PrintWriter(output, true)
        );
        registry.add(session);
        List<String> logs = new ArrayList<>();

        new PlayerMessageListener(session, registry, new TurnInputCoordinator(), logs::add).run();

        assertNull(registry.findByPlayerId(1));
        assertEquals("", output.toString());
        assertEquals(List.of(
                "开始处理客户端：玩家 1",
                "alice 请求退出连接",
                "alice 已从房间移除"
        ), logs);
    }
}
