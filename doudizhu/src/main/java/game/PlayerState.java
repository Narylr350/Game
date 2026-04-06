package game;

import java.util.TreeSet;

/**
 * 玩家状态类。
 * <p>
 * 封装单个玩家在游戏中的所有状态信息,包括玩家ID、名称、手牌、
 * 是否为地主以及在线状态等。
 * </p>
 */
public class PlayerState {
    private final int playerId;              // 玩家唯一标识
    private final String playerName;         // 玩家名称
    private final TreeSet<Integer> cards;    // 玩家手牌(已排序)
    private boolean landlord;                // 是否为地主
    private boolean online;                  // 是否在线

    /**
     * 创建一个新的玩家状态对象。
     * <p>
     * 创建时默认设置玩家为在线状态。
     * </p>
     *
     * @param playerId 玩家唯一标识ID
     * @param playerName 玩家名称
     * @param cards 玩家的初始手牌集合
     */
    public PlayerState(int playerId, String playerName, TreeSet<Integer> cards) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.cards = new TreeSet<>(cards);
        this.online = true;
    }

    /**
     * 获取玩家ID。
     *
     * @return 玩家的唯一标识ID
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * 获取玩家名称。
     *
     * @return 玩家的名称
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * 获取玩家手牌的副本。
     * <p>
     * 返回的是手牌的副本,修改返回的集合不会影响玩家的实际手牌。
     * </p>
     *
     * @return 玩家手牌集合的副本(已排序)
     */
    public TreeSet<Integer> getCards() {
        return new TreeSet<>(cards);
    }

    /**
     * 判断玩家是否为地主。
     *
     * @return 如果是地主返回true,否则返回false
     */
    public boolean isLandlord() {
        return landlord;
    }

    /**
     * 设置玩家的地主状态。
     *
     * @param landlord 是否设置为地主
     */
    public void setLandlord(boolean landlord) {
        this.landlord = landlord;
    }

    /**
     * 判断玩家是否在线。
     *
     * @return 如果在线返回true,否则返回false
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * 设置玩家的在线状态。
     *
     * @param online 是否在线
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * 将额外的牌添加到手牌中。
     * <p>
     * 地主确定后,把底牌并入手牌时会用到此方法。
     * </p>
     *
     * @param extraCards 要添加的额外牌集合
     */
    public void addCards(TreeSet<Integer> extraCards) {
        cards.addAll(extraCards);
    }
}
