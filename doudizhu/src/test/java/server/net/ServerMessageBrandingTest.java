package server.net;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerMessageBrandingTest {

    @Test
    void getMessageIncludesGithubBrandPrefix() {
        assertTrue(Server.getMessage(MessageType.CALL_LANDLORD).contains("ICERainbow666"));
        assertTrue(Server.getMessage(MessageType.ROB_LANDLORD).contains("ICERainbow666"));
        assertTrue(Server.getMessage(MessageType.PLAY_CARD).contains("ICERainbow666"));
    }
}
