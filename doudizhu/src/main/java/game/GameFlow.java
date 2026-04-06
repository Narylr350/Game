package game;

import game.handler.CallLanLordHandler;
import game.handler.RobLandLordHandler;
import util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static rule.NameRule.validatePlayerNames;

/**
 * 游戏流程控制类。
 * <p>
 * 使用单例模式,负责管理整个游戏的生命周期,包括创建房间、发牌、
 * 叫/抢地主流程控制、玩家操作处理等核心游戏逻辑。
 * </p>
 */
public class GameFlow {
    //单例模式
    private static GameFlow gameFlow = new GameFlow();
    private CallLanLordHandler callLanLord;    // 叫地主处理器
    private RobLandLordHandler robLandLord;    // 抢地主处理器

    /**
     * 私有构造函数,初始化叫地主和抢地主的处理器。
     */
    private GameFlow() {
        this.callLanLord = new CallLanLordHandler();
        this.robLandLord = new RobLandLordHandler();
    }

    /**
     * 获取GameFlow的单例实例。
     *
     * @return GameFlow的唯一实例
     */
    public static GameFlow getInstance() {
        return gameFlow;
    }

    /**
     * 收集房间内所有玩家的名称。
     *
     * @param room 游戏房间对象
     * @return 包含所有玩家名称的列表
     */
    private static List<String> collectPlayerNames(GameRoom room) {
        List<String> playerNames = new ArrayList<>();
        for (PlayerState player : room.getPlayers()) {
            playerNames.add(player.getPlayerName());
        }
        return playerNames;
    }

    /**
     * 处理玩家的操作请求。
     * <p>
     * 根据当前游戏阶段,将操作委托给相应的处理器处理。
     * 发牌阶段会触发重新发牌,叫地主和抢地主阶段分别由对应处理器处理,
     * 出牌阶段暂未实现,其他阶段的操作会被拒绝。
     * </p>
     *
     * @param room 游戏房间对象
     * @param playerId 执行操作的玩家ID
     * @param actionType 操作类型(叫/不叫等)
     * @return 操作结果对象,包含操作是否合法、下一步该谁操作等信息
     */
    public GameActionResult handlePlayerAction(GameRoom room, Integer playerId, ActionType actionType) {
        GamePhase currentPhase = room.getPhase();
        GameActionResult result = null;
        if (currentPhase == GamePhase.DEALING) {
            return GameActionResult.redeal("开始重新发牌");
        }
        if (currentPhase == GamePhase.CALL_LANDLORD) {
            result = callLanLord.callLandLordHandler(room, playerId, actionType);
        } else if (currentPhase == GamePhase.ROB_LANDLORD) {
            result = robLandLord.robLandLordHandler(room, playerId, actionType);
        } else if (currentPhase == GamePhase.PLAYING) {

        } else {
            return GameActionResult.invalidAction("当前阶段禁止操作");
        }
        return result;
    }

    /**
     * 创建并开始一个新的游戏房间。
     * <p>
     * 首先验证玩家名称的合法性,然后进行发牌,最后自动进入叫地主阶段。
     * </p>
     *
     * @param playerNames 玩家名称列表
     * @return 初始化完成的游戏房间对象
     * @throws IllegalArgumentException 如果玩家名称不合法
     */
    public GameRoom startRoom(List<String> playerNames) {
        if (!validatePlayerNames(playerNames)) {
            throw new IllegalArgumentException("名字不合法");
        }
        DealResult dealResult = deal(playerNames);
        GameRoom room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        room.setPhase(GamePhase.DEALING);
        room = startCallLandLord(room);
        return room;
    }

    /**
     * 将游戏房间设置为叫地主阶段。
     * <p>
     * 清除地主ID,并随机选择一个玩家开始叫地主。
     * </p>
     *
     * @param room 游戏房间对象
     * @return 已进入叫地主阶段的游戏房间
     */
    public GameRoom startCallLandLord(GameRoom room) {
        room.setPhase(GamePhase.CALL_LANDLORD);
        room.setLandLordId(null);
        //随机一个幸运儿开局
        room.setCurrentTurnPlayerId(new Random().nextInt(1, 4));
        return room;
    }

    /**
     * 重新发牌并创建一个新的游戏房间。
     * <p>
     * 保留原有房间的玩家名称,重新进行发牌并进入叫地主阶段。
     * </p>
     *
     * @param oldRoom 旧的游戏房间对象
     * @return 新创建的游戏房间对象
     */
    public GameRoom reDeal(GameRoom oldRoom) {
        DealResult dealResult = deal(collectPlayerNames(oldRoom));
        GameRoom newRoom = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        newRoom = startCallLandLord(newRoom);
        return newRoom;
    }

    /**
     * 执行开局发牌逻辑。
     * <p>
     * 将54张牌随机分配给3个玩家,每人17张,剩余3张作为底牌。
     * 玩家ID从1开始编号,手牌会自动排序(TreeSet)。
     * </p>
     *
     * @param playerNames 玩家名称列表(必须3个玩家)
     * @return 发牌结果对象,包含所有玩家的手牌和底牌
     */
    public DealResult deal(List<String> playerNames) {
        List<Integer> shuffledDeck = CardUtil.createShuffledDeck();
        List<TreeSet<Integer>> hands = new ArrayList<>();
        TreeSet<Integer> holeCards = new TreeSet<>();

        for (int i = 0; i < 3; i++) {
            hands.add(new TreeSet<>());
        }

        for (int i = 0; i < 3; i++) {
            holeCards.add(shuffledDeck.get(i));
        }

        for (int i = 3; i < shuffledDeck.size(); i++) {
            hands.get((i - 3) % 3)
                    .add(shuffledDeck.get(i));
        }

        List<PlayerState> players = new ArrayList<>();
        for (int i = 0; i < playerNames.size(); i++) {
            players.add(new PlayerState(i + 1, playerNames.get(i), hands.get(i)));
        }

        return new DealResult(players, holeCards);
    }
}
