package server.log;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameLogService {
    private final GameLogRepository repository;
    private final Map<String, Integer> stepCounters = new HashMap<>();

    public GameLogService(GameLogRepository repository) {
        this.repository = repository;
        this.repository.ensureTables();
    }

    public String startSession(List<String> playerNames) {
        String sessionId = UUID.randomUUID().toString();
        repository.insertSession(new GameSessionLog(
                sessionId,
                LocalDateTime.now(),
                null,
                playerNames.get(0),
                playerNames.get(1),
                playerNames.get(2),
                null,
                null,
                null,
                null
        ));
        stepCounters.put(sessionId, 0);
        return sessionId;
    }

    public void appendAction(String sessionId,
                             String phase,
                             int playerId,
                             String playerName,
                             String actionInput,
                             String actionResult,
                             int remainingCardsP1,
                             int remainingCardsP2,
                             int remainingCardsP3) {
        int stepNo = stepCounters.compute(sessionId, (key, current) -> current == null ? 1 : current + 1);
        repository.insertAction(new GameActionLog(
                sessionId,
                stepNo,
                phase,
                playerId,
                playerName,
                actionInput,
                actionResult,
                remainingCardsP1,
                remainingCardsP2,
                remainingCardsP3,
                LocalDateTime.now()
        ));
    }

    public void updateLandlordPlayerId(String sessionId, Integer landlordPlayerId) {
        repository.updateLandlordPlayerId(sessionId, landlordPlayerId);
    }

    public void finishSession(String sessionId,
                              Integer landlordPlayerId,
                              WinnerSide winnerSide,
                              Integer winnerPlayerId,
                              GameEndReason endReason) {
        repository.finishSession(
                sessionId,
                landlordPlayerId,
                winnerSide,
                winnerPlayerId,
                endReason,
                LocalDateTime.now()
        );
        stepCounters.remove(sessionId);
    }
}
