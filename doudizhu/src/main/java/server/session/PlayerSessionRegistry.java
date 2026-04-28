package server.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerSessionRegistry {
    private final List<PlayerSession> sessions = new ArrayList<>();

    public synchronized void add(PlayerSession session) {
        sessions.add(session);
    }

    public synchronized PlayerSession registerAuthenticated(String playerName,
                                                            Socket socket,
                                                            BufferedReader reader,
                                                            PrintWriter writer,
                                                            int maxPlayers) {
        if (sessions.size() >= maxPlayers) {
            return null;
        }

        PlayerSession session = new PlayerSession(sessions.size() + 1, playerName, socket, reader, writer);
        sessions.add(session);
        return session;
    }

    public synchronized void removeByPlayerId(int playerId) {
        sessions.removeIf(session -> session.getPlayerId() == playerId);
    }

    public synchronized PlayerSession findByPlayerId(int playerId) {
        for (PlayerSession session : sessions) {
            if (session.getPlayerId() == playerId) {
                return session;
            }
        }
        return null;
    }

    public synchronized int size() {
        return sessions.size();
    }

    public synchronized List<String> collectPlayerNames() {
        List<String> playerNames = new ArrayList<>();
        for (PlayerSession session : sessions) {
            playerNames.add(session.getPlayerName());
        }
        return playerNames;
    }

    public synchronized List<Integer> collectPlayerIds() {
        List<Integer> playerIds = new ArrayList<>();
        for (PlayerSession session : sessions) {
            playerIds.add(session.getPlayerId());
        }
        return playerIds;
    }

    public synchronized List<PlayerSession> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(sessions));
    }

    public synchronized void broadcast(String message) {
        for (PlayerSession session : sessions) {
            session.send(message);
        }
    }

    public synchronized void broadcastExcept(int excludedPlayerId, String message) {
        for (PlayerSession session : sessions) {
            if (session.getPlayerId() != excludedPlayerId) {
                session.send(message);
            }
        }
    }

    public synchronized void sendToPlayer(int playerId, String message) {
        PlayerSession session = findByPlayerId(playerId);
        if (session != null) {
            session.send(message);
        }
    }

    public synchronized void closeAll() {
        for (PlayerSession session : sessions) {
            try {
                session.close();
            } catch (IOException ignored) {
            }
        }
    }
}
