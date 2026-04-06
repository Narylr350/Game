package server.net;

/**
 * 网络通信结果类。
 * <p>
 * 用于封装服务端与客户端之间的一次通信结果,包含当前操作的玩家ID、
 * 消息内容以及当前的游戏阶段类型。
 * </p>
 */
public class Result {
//    boolean finished;
//    boolean readl;


    Integer currentID;           // 当前操作的玩家ID
    String message;              // 消息内容
    MessageType currentStatus;   // 当前游戏阶段类型


    /**
     * 创建网络通信结果对象。
     *
     * @param playerId 当前操作的玩家ID
     * @param msg 消息内容
     * @param currentStatus 当前游戏阶段类型
     */
    public Result(int playerId, String msg, MessageType currentStatus) {
        this.currentID = playerId;
        this.message = msg;
        this.currentStatus = currentStatus;
    }

    /**
     * 创建网络通信结果对象(不包含阶段类型)。
     *
     * @param playerId 当前操作的玩家ID
     * @param msg 消息内容
     */
    /*public Result(int playerId, String msg) {
    }*/

    /**
     * 获取消息内容。
     *
     * @return 消息内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置消息内容。
     *
     * @param message 新的消息内容
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取当前操作的玩家ID。
     *
     * @return 玩家ID
     */
    public Integer getPlayerId() {
        return currentID;
    }

    /**
     * 获取当前游戏阶段的消息类型。
     *
     * @return 消息类型枚举
     */
    public MessageType getMessageType() {
        return currentStatus;
    }

    /**
     * 设置当前操作的玩家ID。
     *
     * @param currentID 玩家ID
     */
    public void setCurrentID(Integer currentID) {
        this.currentID = currentID;
    }

    public void setCurrentStatus(MessageType currentStatus) {
        this.currentStatus = currentStatus;
    }
}
