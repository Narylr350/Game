package client;

/**
 * 玩家1启动类。
 * <p>
 * 作为第一个玩家的客户端入口,直接委托给GameClient.main()执行。
 * </p>
 */
public class Player1 {
    /**
     * 启动玩家1客户端。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        PlayerClient.main(args);
    }
}
