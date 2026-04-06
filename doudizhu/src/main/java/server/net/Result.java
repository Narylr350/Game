package server.net;

public class Result {
//    boolean finished;
//    boolean readl;


//    Integer nextPlayerID;//下一个玩家ID
//    Integer landlordPlayerID;//地主ID

    Integer currentID;//当前ID
    String message;//当前消息
    MessageType currentStatus;//阶段枚举


    public Result(int playerId, String msg, MessageType currentStatus) {
        this.currentID = playerId;
        this.message = msg;
        this.currentStatus = currentStatus;
    }

    public Result(int playerId, String msg) {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getPlayerId() {
        return currentID;
    }

    public MessageType getMessageType() {
        return currentStatus;
    }

    public void setCurrentID(Integer currentID) {
        this.currentID = currentID;
    }

    public void setCurrentStatus(MessageType currentStatus) {
        this.currentStatus = currentStatus;
    }
}
