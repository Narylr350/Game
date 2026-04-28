package server.log;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GameLogRepository {
    void ensureTables();

    void insertSession(GameSessionLog sessionLog);

    void insertAction(GameActionLog actionLog);

    void finishSession(String sessionId,
                       Integer landlordPlayerId,
                       WinnerSide winnerSide,
                       Integer winnerPlayerId,
                       GameEndReason endReason,
                       LocalDateTime endedAt);

    void updateLandlordPlayerId(String sessionId, Integer landlordPlayerId);

    Optional<GameSessionLog> findSession(String sessionId);
}
