package game.state;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LandlordState {
    // ===== 地主基础 ==
    private final List<Integer> callPassPlayerIds;
    // ===== 地主阶段状态 =====
    private int callPassCount;
    private Integer firstCallerId;
    private Integer landlordCandidateId;

    public LandlordState() {
        this.callPassPlayerIds = new CopyOnWriteArrayList<>();
        this.callPassCount = 0;
        this.firstCallerId = null;
        this.landlordCandidateId = null;
    }

    /**
     * 获取叫地主阶段不叫的次数。
     *
     * @return 不叫次数
     */
    public int getCallPassCount() {
        return callPassCount;
    }

    /**
     * 重置叫地主阶段的不叫次数。
     */
    private void resetCallPassCount() {
        this.callPassCount = 0;
    }

    /**
     * 叫地主不叫次数加1。
     */
    public void incrementCallPassCount() {
        callPassCount++;
    }

    /**
     * 获取叫地主阶段不叫的玩家ID列表。
     *
     * @return 不叫的玩家ID列表
     */
    public List<Integer> getCallPassPlayerIds() {
        return callPassPlayerIds;
    }

    /**
     * 添加叫地主阶段不叫的玩家ID。
     *
     * @param playerId 玩家ID
     */
    public void addCallPassPlayerId(Integer playerId) {
        this.callPassPlayerIds.add(playerId);
    }

    /**
     * 清空叫地主阶段不叫的玩家ID列表。
     */
    private void resetCallPassPlayerIds() {
        callPassPlayerIds.clear();
    }

    /**
     * 获取第一个叫地主的玩家ID。
     *
     * @return 第一个叫地主的玩家ID
     */
    public Integer getFirstCallerId() {
        return firstCallerId;
    }

    /**
     * 设置第一个叫地主的玩家ID。
     *
     * @param firstCallerId 玩家ID
     */
    public void setFirstCallerId(Integer firstCallerId) {
        this.firstCallerId = firstCallerId;
    }

    /**
     * 重置第一个叫地主的玩家ID。
     */
    private void resetFirstCallerId() {
        this.firstCallerId = null;
    }

    /**
     * 重置地主候选人ID。
     */
    public void resetLandlordCandidateId() {
        this.landlordCandidateId = null;
    }


    /**
     * 获取当前地主候选人ID。
     *
     * @return 地主候选人ID
     */
    public Integer getLandlordCandidateId() {
        return landlordCandidateId;
    }

    /**
     * 设置当前地主候选人ID。
     *
     * @param landlordCandidateId 玩家ID
     */
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
