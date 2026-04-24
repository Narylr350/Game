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
public class PlayerClient {
    static Scanner input = new Scanner(System.in);
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
//        final String host = "192.168.214.17";
        final String host = "127.0.0.1";
        final int port = 8888;

        try {
            // ===== 1. 获取玩家输入的名字 =====
            System.out.println("请输入名字");
            String name = scanner.nextLine();

            // ===== 2. 登录/注册逻辑（当前未实现，仅预留结构）=====
            while (true){
                // 这里本应调用 inputselect() 进行登录或注册
                break;  // 当前直接跳过
            }

            // ===== 3. 建立客户端与服务器的Socket连接 =====
            Socket socket = connectServer(host, port);
            if (socket==null){
                System.out.println("连不上");
            }
            System.out.println("已经连接服务器");

            // ===== 4. 创建输出流，用于向服务器发送数据 =====
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // ===== 5. 启动一个独立线程，用于接收服务器消息 =====
            startMessageReader(socket);

            // ===== 6. 连接成功后，先把玩家名字发送给服务器（用于注册玩家）=====
            writer.println(name);

            // ===== 7. 主线程循环：持续读取用户输入并发送给服务器 =====
            while (true) {
                String input = scanner.nextLine();
                writer.println(input);  // 发送给服务器

                // ===== 8. 输入exit时退出客户端 =====
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
            }

            // ===== 9. 关闭资源 =====
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
                    //预留退出
//                    if("GAMEOVER".equals(message)){
//                        System.exit(0);
//                    }
                    System.out.println(message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private static Socket connectServer(String host, int port) {
        while (true) {
            try {
                System.out.println("正在尝试连接服务器...");
                Socket socket = new Socket(host, port);
                System.out.println("服务器连接成功！");
                return socket; // 连接成功，返回socket
            } catch (Exception e) {
                System.out.println("服务器未启动或连接失败，3秒后重试...");
                try {
                    Thread.sleep(3000); // 等待3秒再重连
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    private static void inputselect(){

        while (true){
            System.out.println("1.登录 2.注册 3.exit");
            String select = input.nextLine();
            if ("1".equals(select)){
                //进入登录
                System.out.println("进入登录页面");
            }
            else if ("2".equals(select)){
                //进入注册
                System.out.println("进入注册页面");
            }
            else if ("3".equals(select)||"exit".equals(select)){
                //退出程序
                System.exit(0);
            }
            else {
                System.out.println("重新选择");
            }
        }
    }
}
