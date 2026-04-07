 package server.net; // ICERainbow666
  // ICERainbow666
 /** // ICERainbow666
  * 网络通信结果类。 // ICERainbow666
  * <p> // ICERainbow666
  * 用于封装服务端与客户端之间的一次通信结果，包含当前操作的玩家ID、 // ICERainbow666
  * 消息内容以及当前的游戏阶段类型。 // ICERainbow666
  * </p> // ICERainbow666
  */ // ICERainbow666
 public class Result{ // ICERainbow666
  // ICERainbow666
     /** 当前操作的玩家ID */ // ICERainbow666
     Integer currentID; // ICERainbow666
     /** 消息内容 */ // ICERainbow666
     String message; // ICERainbow666
     /** 当前游戏阶段类型 */ // ICERainbow666
     MessageType currentStatus; // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 创建网络通信结果对象。 // ICERainbow666
      * // ICERainbow666
      * @param playerId 当前操作的玩家ID // ICERainbow666
      * @param msg 消息内容 // ICERainbow666
      * @param currentStatus 当前游戏阶段类型 // ICERainbow666
      */ // ICERainbow666
     public Result(int playerId, String msg, MessageType currentStatus) { // ICERainbow666
         this.currentID = playerId; // ICERainbow666
         this.message = msg; // ICERainbow666
         this.currentStatus = currentStatus; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取消息内容。 // ICERainbow666
      * // ICERainbow666
      * @return 消息内容字符串 // ICERainbow666
      */ // ICERainbow666
     public String getMessage() { // ICERainbow666
         return message; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 设置消息内容。 // ICERainbow666
      * // ICERainbow666
      * @param message 新的消息内容 // ICERainbow666
      */ // ICERainbow666
     public void setMessage(String message) { // ICERainbow666
         this.message = message; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取当前操作的玩家ID。 // ICERainbow666
      * // ICERainbow666
      * @return 玩家ID // ICERainbow666
      */ // ICERainbow666
     public Integer getPlayerId() { // ICERainbow666
         return currentID; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取当前游戏阶段的消息类型。 // ICERainbow666
      * // ICERainbow666
      * @return 消息类型枚举 // ICERainbow666
      */ // ICERainbow666
     public MessageType getMessageType() { // ICERainbow666
         return currentStatus; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 设置当前操作的玩家ID。 // ICERainbow666
      * // ICERainbow666
      * @param currentID 玩家ID // ICERainbow666
      */ // ICERainbow666
     public void setCurrentID(Integer currentID) { // ICERainbow666
         this.currentID = currentID; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 设置当前游戏阶段的消息类型。 // ICERainbow666
      * // ICERainbow666
      * @param currentStatus 消息类型枚举 // ICERainbow666
      */ // ICERainbow666
     public void setCurrentStatus(MessageType currentStatus) { // ICERainbow666
         this.currentStatus = currentStatus; // ICERainbow666
     } // ICERainbow666
 } // ICERainbow666
