package client.net;

import client.auth.ClientAuthState;

public class ServerMessageReader implements Runnable {
    private final ClientConnection connection;
    private final ClientAuthState authState;

    public ServerMessageReader(ClientConnection connection, ClientAuthState authState) {
        this.connection = connection;
        this.authState = authState;
    }

    @Override
    public void run() {
        try {
            String message;
            while ((message = connection.getReader().readLine()) != null) {
                authState.consumeServerMessage(message);
                System.out.println(message);
            }
        } catch (Exception e) {
            if (!connection.isClosed()) {
                e.printStackTrace();
            }
        } finally {
            connection.closeQuietly();
            System.out.println("服务器连接已关闭");
            System.exit(0);
        }
    }
}
