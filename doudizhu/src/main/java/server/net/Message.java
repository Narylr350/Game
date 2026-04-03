package server.net;

public class Message {
    private MessageType type;  // 动作类型
    private String data;      // 具体内容（比如牌）

    public Message(MessageType type, String data) {
        this.type = type;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public String getData() {
        return data;
    }
}