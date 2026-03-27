package server.net;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PlayerConnection {
    private final int playerId;
    private final String name;
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public PlayerConnection(int playerId, String name, Socket socket,
                            BufferedReader reader, PrintWriter writer) {
        this.playerId = playerId;
        this.name = name;
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
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
}
