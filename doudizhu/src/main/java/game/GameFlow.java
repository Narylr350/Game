package game;

import game.action.GameAction;
import game.handler.CallLandlordHandler;
import game.handler.PlayingHandler;
import game.handler.RobLandlordHandler;
import game.state.PlayerState;
import util.CardUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import static rule.NameRuleChecker.validatePlayerNames;

/**
 * 游戏流程管理类。
 * <p>
 * 负责驱动整个游戏的生命周期，包括发牌、叫地主、抢地主和出牌阶段的流转。
 * 该类是游戏的核心调度器，各阶段的规则判断委托给对应的 Handler 处理。
 * </p>
 */
public class GameFlow {

    private final CallLandlordHandler callLandlordHandler;
    private final RobLandlordHandler robLandlordHandler;
    private final PlayingHandler playingHandler;

    public GameFlow() {
        this.callLandlordHandler = new CallLandlordHandler(this);
        this.robLandlordHandler = new RobLandlordHandler(this);
        this.playingHandler = new PlayingHandler();
    }

    /**
     * 从房间中收集所有玩家的名称。
     *
     * @param room 游戏房间对象
     * @return 玩家名称列表
     */
    private static List<String> collectPlayerNames(GameRoom room) {
        List<String> playerNames = new ArrayList<>();
        for (PlayerState player : room.getPlayers()) {
            playerNames.add(player.getPlayerName());
        }
        return playerNames;
    }

    /**
     * 处理玩家的动作请求。
     * <p>
     * 根据房间当前阶段，将请求分发给对应的 Handler 处理。
     * </p>
     *
     * @param room 游戏房间对象
     * @param action 玩家发起的游戏动作
     * @return 动作处理结果
     */
    public GameResult handlePlayerAction(GameRoom room, GameAction action) {
        GamePhase currentPhase = room.getCurrentPhase();
        if (currentPhase == GamePhase.DEALING) {
            return GameResult.redealRequired("开始重新发牌");
        }

        if (currentPhase == GamePhase.CALL_LANDLORD) {
            return callLandlordHandler.handle(room, action);
        }

        if (currentPhase == GamePhase.ROB_LANDLORD) {
            return robLandlordHandler.handle(room, action);
        }

        if (currentPhase == GamePhase.PLAYING) {
            return playingHandler.handle(room, action);
        }

        return GameResult.rejected("当前阶段禁止操作");
    }

    /**
     * 创建房间并初始化游戏。
     * <p>
     * 完成发牌后自动进入叫地主阶段。
     * </p>
     *
     * @param playerNames 玩家名称列表
     * @return 初始化完成的游戏房间
     */
    public GameRoom startRoom(List<String> playerNames) {
        DealResult dealResult = deal(playerNames);
        GameRoom room = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        room.setCurrentPhase(GamePhase.DEALING);
        return startCallLandLord(room);
    }

    /**
     * 进入叫地主阶段。
     * <p>
     * 重置地主状态，并随机选择一个玩家作为起始叫地主的人。
     * </p>
     *
     * @param room 游戏房间对象
     * @return 设置好阶段的游戏房间
     */
    public GameRoom startCallLandLord(GameRoom room) {
        room.setCurrentPhase(GamePhase.CALL_LANDLORD);
        room.setLandlordPlayerId(null);
        // 随机选择起始玩家
        room.setCurrentPlayerId(new Random().nextInt(1, collectPlayerNames(room).size() + 1));
        return room;
    }

    /**
     * 重新发牌。
     * <p>
     * 保留原有玩家，重新洗牌并发牌，然后进入叫地主阶段。
     * </p>
     *
     * @param oldRoom 旧房间对象
     * @return 新房间对象
     */
    public GameRoom reDeal(GameRoom oldRoom) {
        DealResult dealResult = deal(collectPlayerNames(oldRoom));
        GameRoom newRoom = new GameRoom(dealResult.getPlayers(), dealResult.getHoleCards());
        return startCallLandLord(newRoom);
    }

    /**
     * 发牌。
     * <p>
     * 洗牌后留3张作为底牌，剩余牌循环发给3个玩家。
     * </p>
     *
     * @param playerNames 玩家名称列表
     * @return 发牌结果，包含所有玩家手牌和底牌
     */
    public DealResult deal(List<String> playerNames) {
        validatePlayerNames(playerNames);
        List<Integer> shuffledDeck = CardUtil.createShuffledDeck();
        List<TreeSet<Integer>> hands = new ArrayList<>();
        TreeSet<Integer> holeCards = new TreeSet<>();

        for (int i = 0; i < 3; i++) {
            hands.add(new TreeSet<>());
        }

        // 留3张底牌
        for (int i = 0; i < 3; i++) {
            holeCards.add(shuffledDeck.get(i));
        }

        // 剩余牌循环发给3个玩家
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
    /**
     * 确认地主身份并推进到出牌阶段。
     *
     * @param room 游戏房间
     * @param landlordId 地主玩家ID
     */
    public void confirmLandlord(GameRoom room, Integer landlordId) {
        room.setCurrentPhase(GamePhase.PLAYING);
        room.getPlayState().setHighestCardPlayerId(room.getLandlordPlayerId());
        room.setLandlordPlayerId(landlordId);

        PlayerState player = room.getPlayerById(landlordId);
        if (player != null) {
            player.addCards(room.getHoleCards());
        }
    }
}