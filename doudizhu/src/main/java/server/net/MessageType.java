package server.net;


public enum MessageType {
    DEAL_CARDS,     // 发牌
    CALL_LANDLORD,  // 叫地主阶段
    ROB_LANDLORD,   // 抢地主阶段
    PLAY_CARD,      // 出牌
    PASS,           // 不出
    ERROR,          // 出牌错误
    SYSTEM          // 系统消息
}
