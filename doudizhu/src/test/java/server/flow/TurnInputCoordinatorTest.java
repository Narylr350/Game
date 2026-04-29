package server.flow;

import game.enumtype.GamePhase;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnInputCoordinatorTest {

    @Test
    void should_reject_input_from_non_current_player() {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginTurn(1, GamePhase.PLAYING);

        InputAcceptance rejection = coordinator.submit(2, "PASS");

        assertFalse(rejection.accepted());
        assertEquals("现在还没轮到你操作", rejection.message());
        assertNull(coordinator.pollReadyInput());
    }

    @Test
    void should_store_current_player_input_with_waiting_phase() {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginTurn(1, GamePhase.CALL_LANDLORD);

        InputAcceptance acceptance = coordinator.submit(1, "1");

        assertTrue(acceptance.accepted());
        assertEquals("", acceptance.message());

        PlayerInput input = coordinator.pollReadyInput();
        assertEquals(1, input.playerId());
        assertEquals("1", input.message());
        assertEquals(GamePhase.CALL_LANDLORD, input.phase());
    }

    @Test
    void should_wake_waiting_turn_when_current_player_submits() throws Exception {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginTurn(1, GamePhase.PLAYING);

        try (var executor = Executors.newSingleThreadExecutor()) {
            Future<PlayerInput> waiting = executor.submit(coordinator::awaitReadyInput);

            coordinator.submit(1, "PASS");

            PlayerInput input = waiting.get(2, TimeUnit.SECONDS);
            assertEquals(1, input.playerId());
            assertEquals("PASS", input.message());
            assertEquals(GamePhase.PLAYING, input.phase());
        }
    }

    @Test
    void should_accept_replay_votes_from_all_players_in_parallel() {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginReplayVote(Set.of(1, 2, 3));

        InputAcceptance first = coordinator.submit(2, "1");
        InputAcceptance second = coordinator.submit(1, "1");
        InputAcceptance third = coordinator.submit(3, "2");

        assertTrue(first.accepted());
        assertTrue(second.accepted());
        assertTrue(third.accepted());

        Map<Integer, PlayerInput> votes = coordinator.pollReplayVotes();
        assertEquals("1", votes.get(1).message());
        assertEquals("1", votes.get(2).message());
        assertEquals("2", votes.get(3).message());
        assertEquals(GamePhase.SETTLE, votes.get(1).phase());
    }

    @Test
    void should_reject_duplicate_replay_vote() {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginReplayVote(Set.of(1, 2, 3));

        coordinator.submit(1, "1");
        InputAcceptance duplicate = coordinator.submit(1, "2");

        assertFalse(duplicate.accepted());
        assertEquals("你已经选择过了", duplicate.message());
    }

    @Test
    void should_reject_blank_or_invalid_replay_vote_without_recording_it() {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginReplayVote(Set.of(1, 2, 3));

        InputAcceptance blank = coordinator.submit(1, "");
        InputAcceptance invalid = coordinator.submit(1, "abc");
        InputAcceptance valid = coordinator.submit(1, "1");

        assertFalse(blank.accepted());
        assertEquals("请输入 1 继续 或 2 退出", blank.message());
        assertFalse(invalid.accepted());
        assertEquals("请输入 1 继续 或 2 退出", invalid.message());
        assertTrue(valid.accepted());
        assertEquals(Map.of(), coordinator.pollReplayVotes());
    }

    @Test
    void should_wake_when_all_replay_votes_arrive() throws Exception {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginReplayVote(Set.of(1, 2, 3));

        try (var executor = Executors.newSingleThreadExecutor()) {
            Future<Map<Integer, PlayerInput>> waiting = executor.submit(coordinator::awaitReplayVotes);

            coordinator.submit(2, "1");
            coordinator.submit(3, "1");
            coordinator.submit(1, "1");

            Map<Integer, PlayerInput> votes = waiting.get(2, TimeUnit.SECONDS);
            assertEquals(3, votes.size());
            assertEquals("1", votes.get(1).message());
        }
    }

    @Test
    void should_treat_disconnect_as_exit_during_replay_vote() {
        TurnInputCoordinator coordinator = new TurnInputCoordinator();
        coordinator.beginReplayVote(Set.of(1, 2, 3));

        coordinator.submit(1, "1");
        coordinator.submit(2, "1");
        coordinator.handleDisconnect(3);

        Map<Integer, PlayerInput> votes = coordinator.pollReplayVotes();
        assertEquals("2", votes.get(3).message());
        assertEquals(GamePhase.SETTLE, votes.get(3).phase());
    }
}
