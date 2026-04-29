package server.log;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GameLogServiceTest {

    @Test
    void should_create_session_append_actions_and_finish_session() {
        RecordingGameLogRepository repository = new RecordingGameLogRepository();
        GameLogService service = new GameLogService(repository);

        String sessionId = service.startSession(List.of("alice", "bob", "cindy"));

        service.appendAction(
                sessionId,
                "PLAYING",
                2,
                "bob",
                "PASS",
                "不出",
                5,
                8,
                3
        );
        service.finishSession(
                sessionId,
                1,
                WinnerSide.LANDLORD,
                2,
                GameEndReason.NORMAL_SETTLEMENT
        );

        assertNotNull(repository.savedSession);
        assertEquals(sessionId, repository.savedSession.sessionId());
        assertEquals("alice", repository.savedSession.player1Name());
        assertEquals(1, repository.savedActions.size());
        assertEquals(1, repository.savedActions.getFirst().stepNo());
        assertEquals("PASS", repository.savedActions.getFirst().actionInput());
        assertEquals("不出", repository.savedActions.getFirst().actionResult());
        assertEquals(5, repository.savedActions.getFirst().remainingCardsP1());
        assertEquals(8, repository.savedActions.getFirst().remainingCardsP2());
        assertEquals(3, repository.savedActions.getFirst().remainingCardsP3());
        assertEquals(1, repository.finishedLandlordPlayerId);
        assertEquals(WinnerSide.LANDLORD, repository.finishedWinnerSide);
        assertEquals(2, repository.finishedWinnerPlayerId);
        assertEquals(GameEndReason.NORMAL_SETTLEMENT, repository.finishedEndReason);
    }

    @Test
    void should_append_settle_vote_actions_with_player_remaining_counts() {
        RecordingGameLogRepository repository = new RecordingGameLogRepository();
        GameLogService service = new GameLogService(repository);
        String sessionId = service.startSession(List.of("alice", "bob", "cindy"));

        service.appendAction(
                sessionId,
                "SETTLE_VOTE",
                3,
                "cindy",
                "2",
                "退出",
                0,
                5,
                8
        );

        GameActionLog action = repository.savedActions.getFirst();
        assertEquals("SETTLE_VOTE", action.phase());
        assertEquals("2", action.actionInput());
        assertEquals("退出", action.actionResult());
        assertEquals(0, action.remainingCardsP1());
        assertEquals(5, action.remainingCardsP2());
        assertEquals(8, action.remainingCardsP3());
    }

    private static final class RecordingGameLogRepository implements GameLogRepository {
        private GameSessionLog savedSession;
        private final List<GameActionLog> savedActions = new ArrayList<>();
        private Integer finishedLandlordPlayerId;
        private WinnerSide finishedWinnerSide;
        private Integer finishedWinnerPlayerId;
        private GameEndReason finishedEndReason;

        @Override
        public void ensureTables() {
        }

        @Override
        public void insertSession(GameSessionLog sessionLog) {
            this.savedSession = sessionLog;
        }

        @Override
        public void insertAction(GameActionLog actionLog) {
            this.savedActions.add(actionLog);
        }

        @Override
        public void finishSession(String sessionId,
                                  Integer landlordPlayerId,
                                  WinnerSide winnerSide,
                                  Integer winnerPlayerId,
                                  GameEndReason endReason,
                                  LocalDateTime endedAt) {
            this.finishedLandlordPlayerId = landlordPlayerId;
            this.finishedWinnerSide = winnerSide;
            this.finishedWinnerPlayerId = winnerPlayerId;
            this.finishedEndReason = endReason;
        }

        @Override
        public void updateLandlordPlayerId(String sessionId, Integer landlordPlayerId) {
        }

        @Override
        public Optional<GameSessionLog> findSession(String sessionId) {
            return Optional.ofNullable(savedSession);
        }
    }
}
