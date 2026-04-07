 package server.net; // ICERainbow666
  // ICERainbow666
 /** // ICERainbow666
  * 网络消息类。 // ICERainbow666
  * <p> // ICERainbow666
  * 封装服务端与客户端之间的一次通信消息，包含消息类型和数据内容。 // ICERainbow666
  * </p> // ICERainbow666
  */ // ICERainbow666
 public class Message{ // ICERainbow666
     /** 消息类型 */ // ICERainbow666
     private MessageType type; // ICERainbow666
     /** 消息数据（如牌面内容） */ // ICERainbow666
     private String data; // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 创建网络消息对象。 // ICERainbow666
      * // ICERainbow666
      * @param type 消息类型 // ICERainbow666
      * @param data 消息数据内容 // ICERainbow666
      */ // ICERainbow666
     public Message(MessageType type, String data) { // ICERainbow666
         this.type = type; // ICERainbow666
         this.data = data; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取消息类型。 // ICERainbow666
      * // ICERainbow666
      * @return 消息类型枚举 // ICERainbow666
      */ // ICERainbow666
     public MessageType getType() { // ICERainbow666
         return type; // ICERainbow666
     } // ICERainbow666
  // ICERainbow666
     /** // ICERainbow666
      * 获取消息数据内容。 // ICERainbow666
      * // ICERainbow666
      * @return 数据内容字符串 // ICERainbow666
      */ // ICERainbow666
     public String getData() { // ICERainbow666
         return data; // ICERainbow666
     } // ICERainbow666
 } // ICERainbow666
