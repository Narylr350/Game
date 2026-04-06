package game;

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

    public static GameActionResult actionAccepted(String message, Integer currentPlayerId, Integer nextPlayerId) {
        return new GameActionResult(
                true,
                false,
                message,
                nextPlayerId,
                currentPlayerId,
                null,
                null
        );
    }

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
