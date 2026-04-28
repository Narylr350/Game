package server.flow;

import game.enumtype.GamePhase;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TurnInputCoordinator {
    private int currentPlayerId = -1;
    private GamePhase waitingPhase;
    private PlayerInput readyInput;
    private boolean replayVoteMode;
    private boolean interrupted;
    private Set<Integer> replayVoters = Set.of();
    private final Map<Integer, PlayerInput> replayVotes = new LinkedHashMap<>();

    public synchronized void beginTurn(int playerId, GamePhase phase) {
        replayVoteMode = false;
        replayVoters = Set.of();
        replayVotes.clear();
        interrupted = false;
        currentPlayerId = playerId;
        waitingPhase = phase;
        readyInput = null;
    }

    public synchronized void beginReplayVote(Set<Integer> playerIds) {
        replayVoteMode = true;
        interrupted = false;
        replayVoters = Set.copyOf(playerIds);
        replayVotes.clear();
        currentPlayerId = -1;
        waitingPhase = GamePhase.SETTLE;
        readyInput = null;
    }

    public synchronized InputAcceptance submit(int playerId, String message) {
        if (replayVoteMode) {
            if (!replayVoters.contains(playerId)) {
                return InputAcceptance.reject("当前不在本局结算选择中");
            }
            if (replayVotes.containsKey(playerId)) {
                return InputAcceptance.reject("你已经选择过了");
            }
            replayVotes.put(playerId, new PlayerInput(playerId, message, GamePhase.SETTLE));
            if (replayVotes.size() == replayVoters.size()) {
                notifyAll();
            }
            return InputAcceptance.accept();
        }
        if (playerId != currentPlayerId) {
            return InputAcceptance.reject("现在还没轮到你操作");
        }
        readyInput = new PlayerInput(playerId, message, waitingPhase);
        notifyAll();
        return InputAcceptance.accept();
    }

    public synchronized PlayerInput pollReadyInput() {
        PlayerInput input = readyInput;
        readyInput = null;
        if (input != null) {
            currentPlayerId = -1;
            waitingPhase = null;
        }
        return input;
    }

    public synchronized PlayerInput awaitReadyInput() {
        while (readyInput == null && !interrupted) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        if (interrupted) {
            interrupted = false;
            return null;
        }
        return pollReadyInput();
    }

    public synchronized Map<Integer, PlayerInput> pollReplayVotes() {
        if (!replayVoteMode || replayVotes.size() != replayVoters.size()) {
            return Map.of();
        }

        Map<Integer, PlayerInput> result = new LinkedHashMap<>(replayVotes);
        replayVoteMode = false;
        replayVoters = Set.of();
        replayVotes.clear();
        waitingPhase = null;
        return result;
    }

    public synchronized Map<Integer, PlayerInput> awaitReplayVotes() {
        while (replayVotes.size() < replayVoters.size() && !interrupted) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Map.of();
            }
        }
        if (interrupted) {
            interrupted = false;
            replayVoteMode = false;
            replayVoters = Set.of();
            replayVotes.clear();
            waitingPhase = null;
            return Map.of();
        }
        return pollReplayVotes();
    }

    public synchronized void handleDisconnect(int playerId) {
        if (replayVoteMode && replayVoters.contains(playerId) && !replayVotes.containsKey(playerId)) {
            replayVotes.put(playerId, new PlayerInput(playerId, "2", GamePhase.SETTLE));
            if (replayVotes.size() == replayVoters.size()) {
                notifyAll();
            }
            return;
        }

        if (!replayVoteMode && playerId == currentPlayerId) {
            interrupted = true;
            readyInput = null;
            currentPlayerId = -1;
            waitingPhase = null;
            notifyAll();
        }
    }

    public synchronized int getCurrentPlayerId() {
        return currentPlayerId;
    }
}
