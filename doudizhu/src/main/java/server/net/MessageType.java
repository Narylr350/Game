 package server.net;
 
 /**
  * 消息类型枚举。
  * <p>
  * 定义服务端与客户端通信的消息类型，用于标识当前消息的业务场景。
  * </p>
  */
 public enum MessageType{
     /** 发牌 */
     DEAL_CARDS,
     /** 叫地主阶段 */
     CALL_LANDLORD,
     /** 抢地主阶段 */
     ROB_LANDLORD,
     /** 出牌 */
     PLAY_CARD,
     /** 不出/过牌 */
     PASS,
     /** 游戏结束 */
     GAME_OVER,
     /** 系统消息 */
     SYSTEM
 }
