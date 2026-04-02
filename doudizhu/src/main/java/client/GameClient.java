package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class GameClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        final String host = "192.168.10.2";
        final int port = 8888;

        try {
            System.out.println("请输入名字");
            String name = scanner.nextLine();

            Socket socket = new Socket(host, port);
            System.out.println("已经连接服务器");

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            startMessageReader(socket);

            writer.println(name);

            System.out.println("请输入要发送的内容：");
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

    // 客户端开启监听线程，接收服务端消息并显示
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
