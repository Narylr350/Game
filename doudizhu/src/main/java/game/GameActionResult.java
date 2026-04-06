package game;

/**
 * 游戏操作结果封装类。
 * <p>
 * 用于封装玩家操作后的结果,包括操作是否合法、是否需要重新发牌、
 * 下一位行动玩家、消息提示等信息。该类提供了多个静态工厂方法来
 * 快速创建不同场景下的操作结果。
 * </p>
 */
public class GameActionResult {
    private boolean valid;                 // 是否合法操作
    private boolean needRedeal;           // 是否需要重新发牌
    private String displayMessage;        // 给客户端的提示信息
    private Integer nextTurnPlayerId;     // 下一位轮到的玩家
    private Integer sendToPlayerId;       // 发给谁（null=广播）
    private Integer finalLandLordId;      // 最终地主
    private GamePhase nextPhase;          // 操作后的阶段

    public GameActionResult(boolean valid, boolean needRedeal, String displayMessage, Integer nextTurnPlayerId, Integer sendToPlayerId, Integer finalLandLordId, GamePhase nextPhase) {
        this.valid = valid;
        this.needRedeal = needRedeal;
        this.displayMessage = displayMessage;
        this.nextTurnPlayerId = nextTurnPlayerId;
        this.sendToPlayerId = sendToPlayerId;
        this.finalLandLordId = finalLandLordId;
        this.nextPhase = nextPhase;
    }

    /**
     * 创建一个不合法操作的结果对象。
     * <p>
     * 用于表示玩家的操作不合法,不会改变游戏状态,也不会导致重新发牌。
     * 结果会广播给所有玩家。
     * </p>
     *
     * @param message 错误提示信息
     * @return 表示不合法操作的游戏结果对象
     */
    public static GameActionResult invalidAction(String message) {
        return new GameActionResult(
                false,
                false,
                message,
                null,
                null,
                null,
                null
        );
    }

    /**
     * 创建一个不合法操作的结果对象,并指定接收玩家。
     * <p>
     * 用于表示玩家的操作不合法,结果只会发送给指定的玩家。
     * </p>
     *
     * @param message 错误提示信息
     * @param sendToPlayerId 接收该消息的玩家ID
     * @return 表示不合法操作的游戏结果对象
     */
    public static GameActionResult invalidAction(String message, Integer sendToPlayerId) {
        return new GameActionResult(
                false,
                false,
                message,
                null,
                sendToPlayerId,
                null,
                null
        );
    }


    /**
     * 创建一个合法操作的结果对象,并指定下一位行动的玩家。
     * <p>
     * 用于表示玩家的操作合法有效,游戏继续进行,轮到指定的玩家行动。
     * 结果会广播给所有玩家。
     * </p>
     *
     * @param message 操作成功的提示信息
     * @param nextPlayerId 下一位行动的玩家ID
     * @return 表示操作成功接受的游戏结果对象
     */
    public static GameActionResult actionAccepted(String message, Integer nextPlayerId) {
        return new GameActionResult(
                true,
                false,
                message,
                nextPlayerId,
                null,
                null,
                null
        );
    }
    /**
     * 创建一个合法操作的结果对象,并指定下一位行动的玩家和消息接收者。
     * <p>
     * 用于表示玩家的操作合法有效,结果只会发送给指定的玩家。
     * </p>
     *
     * @param message 操作成功的提示信息
     * @param nextTurnPlayerId 下一位行动的玩家ID
     * @param sendToPlayerId 接收该消息的玩家ID
     * @return 表示操作成功接受的游戏结果对象
     */
    public static GameActionResult actionAccepted(String message, Integer nextTurnPlayerId, Integer sendToPlayerId) {
        return new GameActionResult(
                true,
                false,
                message,
                nextTurnPlayerId,
                sendToPlayerId,
                null,
                null
        );
    }

    /**
     * 创建一个合法操作的结果对象,不指定下一位玩家。
     * <p>
     * 用于表示玩家的操作合法有效,但不改变行动顺序。
     * 结果会广播给所有玩家。
     * </p>
     *
     * @param message 操作成功的提示信息
     * @return 表示操作成功接受的游戏结果对象
     */
    public static GameActionResult actionAccepted(String message) {
        return new GameActionResult(
                true,
                false,
                message,
                null,
                null,
                null,
                null
        );
    }

    /**
     * 创建一个需要重新发牌的结果对象。
     * <p>
     * 用于表示当前牌局需要重新开始发牌,游戏阶段会重置为发牌阶段(DEALING)。
     * 结果会广播给所有玩家。
     * </p>
     *
     * @param message 重新发牌的提示信息
     * @return 表示需要重新发牌的游戏结果对象
     */
    public static GameActionResult redeal(String message) {
        return new GameActionResult(
                true,
                true,
                message,
                null,
                null,
                null,
                GamePhase.DEALING
        );
    }

    /**
     * 创建一个地主已确定的结果对象。
     * <p>
     * 用于表示叫地主阶段结束,地主身份已确定,游戏进入出牌阶段(PLAYING)。
     * 结果会广播给所有玩家。
     * </p>
     *
     * @param message 地主确定的提示信息
     * @param landlordId 地主的玩家ID
     * @return 表示地主已确定的游戏结果对象
     */
    public static GameActionResult landLordDecided(String message, Integer landlordId) {
        return new GameActionResult(
                true,
                false,
                message,
                null,
                null,
                landlordId,
                GamePhase.PLAYING
        );
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isNeedRedeal() {
        return needRedeal;
    }

    public void setNeedRedeal(boolean needRedeal) {
        this.needRedeal = needRedeal;
    }

    public String getDisplayMessage() {
        return displayMessage;
    }

    public void setDisplayMessage(String displayMessage) {
        this.displayMessage = displayMessage;
    }

    public Integer getNextTurnPlayerId() {
        return nextTurnPlayerId;
    }

    public void setNextTurnPlayerId(Integer nextTurnPlayerId) {
        this.nextTurnPlayerId = nextTurnPlayerId;
    }

    public Integer getSendToPlayerId() {
        return sendToPlayerId;
    }

    public void setSendToPlayerId(Integer sendToPlayerId) {
        this.sendToPlayerId = sendToPlayerId;
    }

    public Integer getFinalLandlordId() {
        return finalLandLordId;
    }

    public void setFinalLandlordId(Integer finalLandLordId) {
        this.finalLandLordId = finalLandLordId;
    }

    public GamePhase getNextPhase() {
        return nextPhase;
    }

    public void setNextPhase(GamePhase nextPhase) {
        this.nextPhase = nextPhase;
    }
}
