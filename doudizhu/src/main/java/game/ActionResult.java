package game;

public class ActionResult {
    private boolean success;
    private boolean reDeal;
    private String message;
    private Integer nextPlayerId;
    private Integer sendMessageByPlayerId;
    private Integer landlordId;
    private GamePhase phase;

    public ActionResult(boolean success, boolean reDeal, String message, Integer nextPlayerId, Integer sendMessageByPlayerId, Integer landlordId, GamePhase phase) {
        this.success = success;
        this.reDeal = reDeal;
        this.message = message;
        this.nextPlayerId = nextPlayerId;
        this.sendMessageByPlayerId = sendMessageByPlayerId;
        this.landlordId = landlordId;
        this.phase = phase;
    }

    //抢地主失败
    public static ActionResult fail(String message) {
        ActionResult result = new ActionResult(
                false,
                false,
                message,
                null,
                null,
                null,
                null
        );
        return result;
    }

    //抢地主成功
    public static ActionResult success(String message, Integer sendMessageByPlayerId, Integer landlordId) {
        ActionResult result = new ActionResult(
                true,
                false,
                message,
                null,
                sendMessageByPlayerId,
                landlordId,
                null
        );
        return result;
    }

    //需要重开
    public static ActionResult success(String message, boolean reDeal) {
        ActionResult result = new ActionResult(
                true,
                true,
                message,
                null,
                null,
                null,
                null
        );
        return result;
    }

    //返回是地主的结果
    public static ActionResult isLandLord(String message, Integer landlordId) {
        ActionResult result = new ActionResult(
                true,
                false,
                message,
                null,
                null,
                landlordId,
                GamePhase.PLAYING
        );
        return result;
    }

    //返回不是地主的结果
    public static ActionResult notLandLord(String message) {
        ActionResult result = new ActionResult(
                true,
                true,
                message,
                null,
                null,
                null,
                GamePhase.PLAYING
        );
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isReDeal() {
        return reDeal;
    }

    public void setReDeal(boolean reDeal) {
        this.reDeal = reDeal;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getNextPlayerId() {
        return nextPlayerId;
    }

    public void setNextPlayerId(Integer nextPlayerId) {
        this.nextPlayerId = nextPlayerId;
    }

    public Integer getSendMessageByPlayerId() {
        return sendMessageByPlayerId;
    }

    public void setSendMessageByPlayerId(Integer sendMessageByPlayerId) {
        this.sendMessageByPlayerId = sendMessageByPlayerId;
    }

    public Integer getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(Integer landlordId) {
        this.landlordId = landlordId;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public void setPhase(GamePhase phase) {
        this.phase = phase;
    }
}
