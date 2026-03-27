package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Player2 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("请输入名字");
            String name = scanner.nextLine();

            Socket socket = new Socket("127.0.0.1", 8888);
            System.out.println("已经连接服务器");

            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // 先启动接收线程，只启动一次
            handleServer(socket);

            // 发送身份信息
            writer.println(name + " 已经连接");

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleServer(Socket socket) {
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                String msg;
                while ((msg = reader.readLine()) != null) {
                    System.out.println(msg);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}