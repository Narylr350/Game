package game;

/**
 * 游戏动作处理结果类。
 * <p>
 * 封装服务端对玩家动作的处理结果，包含成功状态、消息、事件类型和目标玩家ID。
 * 该类为不可变对象，创建后无法修改。
 * </p>
 */
public class GameResult {
    private final boolean success;
    private final String message;
    private final GameEventType eventType;
    private final Integer sendToPlayerId;

    /**
     * 创建游戏动作结果对象。
     *
     * @param success 是否成功
     * @param message 结果消息
     * @param eventType 事件类型
     * @param sendToPlayerId 目标玩家ID
     */
    public GameResult(boolean success, String message, GameEventType eventType, Integer sendToPlayerId) {
        this.success = success;
        this.message = message;
        this.eventType = eventType;
        this.sendToPlayerId = sendToPlayerId;
    }

    /**
     * 创建动作被拒绝的结果。
     *
     * @param message 拒绝原因
     * @return 被拒绝的结果对象
     */
    public static GameResult rejected(String message) {
        return new GameResult(
                false,
                message,
                GameEventType.ACTION_REJECTED,
                null);
    }

    /**
     * 创建动作被拒绝的结果，并指定接收消息的玩家。
     *
     * @param message 拒绝原因
     * @param sendToPlayerId 接收消息的玩家ID
     * @return 被拒绝的结果对象
     */
    public static GameResult rejected(String message, Integer sendToPlayerId) {
        return new GameResult(
                    false,
                message,
                GameEventType.ACTION_REJECTED,
                sendToPlayerId);
    }

    /**
     * 创建动作被接受的结果。
     *
     * @param message 成功消息
     * @return 被接受的结果对象
     */
    public static GameResult accepted(String message) {
        return new GameResult(
                true,
                message,
                GameEventType.ACTION_ACCEPTED,
                null);
    }

    /**
     * 创建动作被接受的结果，并指定下一个操作的玩家。
     *
     * @param message 成功消息
     * @param sendToPlayerId 下一个操作的玩家ID
     * @return 被接受的结果对象
     */
    public static GameResult accepted(String message, Integer sendToPlayerId) {
        return new GameResult(
                true,
                message,
                GameEventType.ACTION_ACCEPTED,
                sendToPlayerId);
    }

    /**
     * 创建需要重新发牌的结果。
     *
     * @param message 重发原因
     * @return 重新发牌的结果对象
     */
    public static GameResult redealRequired(String message) {
        return new GameResult(
                true,
                message,
                GameEventType.REDEAL_REQUIRED,
                null);
    }

    /**
     * 创建地主已确定的结果。
     *
     * @param message 结果消息
     * @return 地主确定的结果对象
     */
    public static GameResult landlordDecided(String message) {
        return new GameResult(
                true,
                message,
                GameEventType.LANDLORD_DECIDED,
                null);
    }

    public static GameResult highestCardDecided(String message){
        return new GameResult(
                true,
                message,
                GameEventType.HIGHESTCARD_DECIDED,
                null
        );
    }
    /**
     * 判断动作是否被成功处理。
     *
     * @return 成功返回true，否则返回false
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 获取结果消息。
     *
     * @return 消息内容
     */
    public String getMessage() {
        return message;
    }

    /**
     * 获取事件类型。
     *
     * @return 游戏事件类型
     */
    public GameEventType getEventType() {
        return eventType;
    }

    /**
     * 获取目标玩家ID。
     *
     * @return 目标玩家ID
     */
    public Integer getSendToPlayerId() {
        return sendToPlayerId;
    }
}