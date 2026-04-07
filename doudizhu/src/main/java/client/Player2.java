package client;

/**
 * 玩家2启动类。
 * <p>
 * 作为第二个玩家的客户端入口,直接委托给GameClient.main()执行。
 * </p>
 */
public class Player2 {
    /**
     * 启动玩家2客户端。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        PlayerClient.main(args);
    }
}
