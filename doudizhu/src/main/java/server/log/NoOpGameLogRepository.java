package server.log;

import java.time.LocalDateTime;
import java.util.Optional;

public class NoOpGameLogRepository implements GameLogRepository {
    @Override
    public void ensureTables() {
    }

    @Override
    public void insertSession(GameSessionLog sessionLog) {
    }

    @Override
    public void insertAction(GameActionLog actionLog) {
    }

    @Override
    public void finishSession(String sessionId,
                              Integer landlordPlayerId,
                              WinnerSide winnerSide,
                              Integer winnerPlayerId,
                              GameEndReason endReason,
                              LocalDateTime endedAt) {
    }

    @Override
    public void updateLandlordPlayerId(String sessionId, Integer landlordPlayerId) {
    }

    @Override
    public Optional<GameSessionLog> findSession(String sessionId) {
        return Optional.empty();
    }
}
