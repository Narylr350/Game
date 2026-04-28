package client.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ClientConnection {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;
    private volatile boolean closed;

    private ClientConnection(Socket socket, BufferedReader reader, PrintWriter writer) {
        this.socket = socket;
        this.reader = reader;
        this.writer = writer;
    }

    public static ClientConnection connect(String host, int port) {
        while (true) {
            try {
                System.out.println("正在尝试连接服务器...");
                Socket socket = new Socket(host, port);
                System.out.println("服务器连接成功！");
                return new ClientConnection(
                        socket,
                        new BufferedReader(new InputStreamReader(socket.getInputStream())),
                        new PrintWriter(socket.getOutputStream(), true)
                );
            } catch (Exception e) {
                System.out.println("服务器未启动或连接失败，3秒后重试...");
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        }
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void send(String message) {
        if (closed) {
            return;
        }
        writer.println(message);
    }

    public synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        writer.close();
        reader.close();
        socket.close();
    }

    public void closeQuietly() {
        try {
            close();
        } catch (IOException ignored) {
        }
    }

    public boolean isClosed() {
        return closed;
    }
}
