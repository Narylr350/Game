package server.log;

import java.time.LocalDateTime;

public record GameActionLog(
        String sessionId,
        int stepNo,
        String phase,
        int playerId,
        String playerName,
        String actionInput,
        String actionResult,
        int remainingCardsP1,
        int remainingCardsP2,
        int remainingCardsP3,
        LocalDateTime createdAt
) {
}
