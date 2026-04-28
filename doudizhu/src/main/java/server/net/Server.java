package server.net;

import server.bootstrap.ServerApplication;

/**
 * 斗地主游戏服务端入口类。
 * <p>
 * 兼容原有启动入口，具体装配和流程转到新的服务端应用中。
 * </p>
 */
public class Server {

    public static void main(String[] args) {
        new ServerApplication().start(8888, 3);
    }
}
