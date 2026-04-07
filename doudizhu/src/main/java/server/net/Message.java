package server.net;

/**
 * 网络消息类。
 * <p>
 * 封装服务端与客户端之间的一次通信消息，包含消息类型和数据内容。
 * </p>
 */
public class Message{
    /** 消息类型 */
    private MessageType type;
    /** 消息数据（如牌面内容） */
    private String data;

    /**
     * 创建网络消息对象。
     *
     * @param type 消息类型
     * @param data 消息数据内容
     */
    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    /**
     * 获取消息类型。
     *
     * @return 消息类型枚举
     */
    public MessageType getType() {
        return type;
    }

    /**
     * 获取消息数据内容。
     *
     * @return 数据内容字符串
     */
    public String getData() {
        return data;
    }
}
