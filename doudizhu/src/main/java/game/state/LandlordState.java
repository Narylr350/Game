package game.state;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LandlordState {
    private final List<Integer> callPassPlayerIds;    // 叫地主不叫的玩家ID列表
    private int callPassCount;                        // 叫地主不叫的次数
    private Integer firstCallerId;                    // 第一个叫地主的玩家ID
    private Integer landlordCandidateId;              // 地主候选人ID

    public LandlordState() {
        this.callPassPlayerIds = new CopyOnWriteArrayList<>();
        this.callPassCount = 0;
        this.firstCallerId = null;
        this.landlordCandidateId = null;
    }

    public int getCallPassCount() {
        return callPassCount;
    }

    private void resetCallPassCount() {
        this.callPassCount = 0;
    }

    public void incrementCallPassCount() {
        callPassCount++;
    }

    public List<Integer> getCallPassPlayerIds() {
        return callPassPlayerIds;
    }

    public void addCallPassPlayerId(Integer playerId) {
        this.callPassPlayerIds.add(playerId);
    }

    private void resetCallPassPlayerIds() {
        callPassPlayerIds.clear();
    }

    public Integer getFirstCallerId() {
        return firstCallerId;
    }

    public void setFirstCallerId(Integer firstCallerId) {
        this.firstCallerId = firstCallerId;
    }

    private void resetFirstCallerId() {
        this.firstCallerId = null;
    }

    public void resetLandlordCandidateId() {
        this.landlordCandidateId = null;
    }

    public Integer getLandlordCandidateId() {
        return landlordCandidateId;
    }

    public void setLandlordCandidateId(Integer landlordCandidateId) {
        this.landlordCandidateId = landlordCandidateId;
    }

    /**
     * 重置叫地主阶段的所有相关状态。
     * <p>
     * 该方法会依次调用以下方法，清空叫地主阶段的全部状态：
     * <ul>
     *   <li>{@link #resetCallPassCount()} - 重置叫地主不叫的次数</li>
     *   <li>{@link #resetCallPassPlayerIds()} - 清空叫地主不叫的玩家ID列表</li>
     *   <li>{@link #resetFirstCallerId()} - 重置第一个叫地主的玩家ID</li>
     *   <li>{@link #resetLandlordCandidateId()} - 重置地主候选人ID</li>
     * </ul>
     * <p>
     * 通常在以下场景调用：
     * <ul>
     *   <li>重新开始叫地主阶段</li>
     *   <li>一轮叫地主结束后无人成为地主</li>
     * </ul>
     */
    public void resetLandlordPhaseState() {
        resetCallPassCount();
        resetCallPassPlayerIds();
        resetFirstCallerId();
        resetLandlordCandidateId();
    }

}
