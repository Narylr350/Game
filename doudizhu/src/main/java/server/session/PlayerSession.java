package server.session;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerSession {
    private final int playerId;
    private final String playerName;
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public PlayerSession(int playerId, String playerName, Socket socket, BufferedReader reader, PrintWriter writer) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public void send(String message) {
        writer.println(message);
    }

    public void close() throws IOException {
        writer.close();
        reader.close();
        socket.close();
    }
}
