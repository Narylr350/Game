package server.log;

import java.time.LocalDateTime;

public record GameSessionLog(
        String sessionId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String player1Name,
        String player2Name,
        String player3Name,
        Integer landlordPlayerId,
        Integer winnerPlayerId,
        WinnerSide winnerSide,
        GameEndReason endReason
) {
}
