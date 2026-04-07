 package server.net; // ICERainbow666
  // ICERainbow666
 /** // ICERainbow666
  * 消息类型枚举。 // ICERainbow666
  * <p> // ICERainbow666
  * 定义服务端与客户端通信的消息类型，用于标识当前消息的业务场景。 // ICERainbow666
  * </p> // ICERainbow666
  */ // ICERainbow666
 public enum MessageType{ // ICERainbow666
     /** 发牌 */ // ICERainbow666
     DEAL_CARDS, // ICERainbow666
     /** 叫地主阶段 */ // ICERainbow666
     CALL_LANDLORD, // ICERainbow666
     /** 抢地主阶段 */ // ICERainbow666
     ROB_LANDLORD, // ICERainbow666
     /** 出牌 */ // ICERainbow666
     PLAY_CARD, // ICERainbow666
     /** 不出/过牌 */ // ICERainbow666
     PASS, // ICERainbow666
     /** 出牌错误 */ // ICERainbow666
     ERROR, // ICERainbow666
     /** 系统消息 */ // ICERainbow666
     SYSTEM // ICERainbow666
 } // ICERainbow666
