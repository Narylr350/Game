package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 斗地主游戏客户端类。
 * <p>
 * 负责与服务端建立连接,接收服务端消息并发送用户输入到服务端。
 * 客户端启动后会提示输入玩家名称,然后进入消息收发循环。
 * </p>
 */
public class GameClient {
    /**
     * 客户端主方法。
     * <p>
     * 连接到服务端(默认地址127.0.0.1:8888),启动消息接收线程,
     * 然后在控制台循环读取用户输入并发送到服务端。
     * 输入"exit"可退出客户端。
     * </p>
     *
     * @param args 命令行参数(未使用)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        final String host = "127.0.0.1";
        final int port = 8888;

        try {
            System.out.println("请输入名字");
            String name = scanner.nextLine();

            Socket socket = new Socket(host, port);
            System.out.println("已经连接服务器");

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            startMessageReader(socket);

            writer.println(name);

            while (true) {
                String input = scanner.nextLine();
                writer.println(input);

                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
            }

            writer.close();
            socket.close();
            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 客户端开启监听线程，接收服务端消息并显示。
     * <p>
     * 创建一个后台线程持续读取服务端的输入流,
     * 将接收到的消息打印到控制台。
     * </p>
     *
     * @param socket 已连接的服务端Socket
     */
    private static void startMessageReader(Socket socket) {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
