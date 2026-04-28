package client;

import client.bootstrap.ClientApplication;

/**
 * 斗地主游戏客户端类。
 * <p>
 * 保留原有启动入口，具体连接、收发和控制台交互转到新的客户端应用中。
 * </p>
 */
public class PlayerClient {

    public static void main(String[] args) {
        new ClientApplication().start("127.0.0.1", 8888);
    }
}
