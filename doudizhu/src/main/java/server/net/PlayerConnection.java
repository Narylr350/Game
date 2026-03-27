package server.net;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class PlayerConnection {
    private int playerId;
    private String name;
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private List<Integer> cards;

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

    public List<Integer> getCards() {
        return cards;
    }

    public void setCards(List<Integer> cards) {
        this.cards = cards;
    }
}
