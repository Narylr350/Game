package server.session;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerSessionRegistryTest {

    @Test
    void should_collect_player_names_in_join_order() {
        PlayerSessionRegistry registry = new PlayerSessionRegistry();
        registry.add(createSession(1, "alice"));
        registry.add(createSession(2, "bob"));

        assertEquals(List.of("alice", "bob"), registry.collectPlayerNames());
    }

    @Test
    void should_remove_player_by_id() {
        PlayerSessionRegistry registry = new PlayerSessionRegistry();
        registry.add(createSession(1, "alice"));
        registry.add(createSession(2, "bob"));

        registry.removeByPlayerId(1);

        assertNull(registry.findByPlayerId(1));
        assertEquals(1, registry.size());
    }

    private PlayerSession createSession(int playerId, String name) {
        return new PlayerSession(
                playerId,
                name,
                new Socket(),
                new BufferedReader(new StringReader("")),
                new PrintWriter(new StringWriter(), true)
        );
    }
}
