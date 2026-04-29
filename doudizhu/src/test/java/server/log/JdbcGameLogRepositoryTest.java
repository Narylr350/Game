package server.log;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JdbcGameLogRepositoryTest {

    @Test
    void should_expose_expected_table_names() throws Exception {
        Method sessionTable = JdbcGameLogRepository.class.getDeclaredMethod("sessionTableName");
        Method actionTable = JdbcGameLogRepository.class.getDeclaredMethod("actionTableName");

        assertEquals("game_session_log", sessionTable.invoke(null));
        assertEquals("game_action_log", actionTable.invoke(null));
    }
}
