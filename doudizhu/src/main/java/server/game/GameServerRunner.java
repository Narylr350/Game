package server.game;

import game.action.ActionType;
import game.action.GameAction;
import game.enumtype.GamePhase;
import game.flow.GameFlow;
import game.model.GameResult;
import game.model.GameRoom;
import game.state.LandlordState;
import game.state.PlayerState;
import server.flow.PlayerInput;
import server.flow.TurnInputCoordinator;
import server.log.GameEndReason;
import server.log.GameLogService;
import server.log.JdbcGameLogRepository;
import server.log.NoOpGameLogRepository;
import server.log.WinnerSide;
import server.session.PlayerSession;
import server.session.PlayerSessionRegistry;
import util.CardUtil;
import util.GamePromptUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameServerRunner {
    private final PlayerSessionRegistry registry;
    private final TurnInputCoordinator coordinator;
    private final GameFlow gameFlow;
    private final GameLogService gameLogService;

    private GameRoom currentRoom;
    private LandlordState landlordState;
    private String currentSessionId;
    private Integer settledWinnerPlayerId;

    public GameServerRunner(PlayerSessionRegistry registry, TurnInputCoordinator coordinator) {
        this(registry, coordinator, createGameLogService());
    }

    GameServerRunner(PlayerSessionRegistry registry,
                     TurnInputCoordinator coordinator,
                     GameLogService gameLogService) {
        this.registry = registry;
        this.coordinator = coordinator;
        this.gameFlow = new GameFlow();
        this.gameLogService = gameLogService;
    }

    public void run() {
        currentRoom = gameFlow.startRoom(registry.collectPlayerNames());
        landlordState = currentRoom.getLandlordState();
        settledWinnerPlayerId = null;
        startSessionLogging();
        logRoomState("房间创建完成");
        logServer("底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));
        sendOpeningHands(currentRoom);

        while (true) {
            if (currentRoom == null) {
                logServer("当前房间为空，流程结束");
                return;
            }

            GamePhase gamePhase = currentRoom.getCurrentPhase();
            if (gamePhase == GamePhase.SETTLE) {
                if (!handleReplayPhase()) {
                    return;
                }
                continue;
            }

            Integer playerId = currentRoom.getCurrentPlayerId();
            if (playerId == null) {
                logServer("当前没有可操作玩家，流程结束");
                return;
            }

            logTurnStart(playerId, gamePhase);

            PlayerInput result = waitPlayerAction(playerId, gamePhase);
            if (result == null) {
                logServer("等待玩家输入失败，流程结束");
                safelyLog(() -> finishCurrentSession(GameEndReason.PLAYER_DISCONNECTED, settledWinnerPlayerId), "玩家掉线收尾");
                shutdownRoom();
                return;
            }

            ActionType actionType = ActionType.parseAction(result.message(), gamePhase);
            if (actionType == null) {
                registry.sendToPlayer(playerId, "输入无法识别，请重新输入");
                logServer("玩家 " + playerId + " 输入无法识别：" + result.message());
                continue;
            }

            GameAction action;
            if (gamePhase == GamePhase.PLAYING) {
                action = buildPlayingAction(playerId, actionType, result.message());
                if (action == null) {
                    continue;
                }

                GameResult gameResult = gameFlow.handlePlayerAction(currentRoom, action);
                handlePlayingResult(playerId, result.message(), action, gameResult);
                if (gameResult != null && gameResult.isGameSettled()) {
                    continue;
                }
            } else {
                action = new GameAction(result.playerId(), actionType, null);
            }

            if (!handleGameResult(playerId, result.message(), action)) {
                return;
            }
        }
    }

    private PlayerInput waitPlayerAction(int playerId, GamePhase phase) {
        coordinator.beginTurn(playerId, phase);
        PlayerSession session = registry.findByPlayerId(playerId);
        String playerName = session == null ? "" : session.getPlayerName();
        registry.broadcast(GamePromptUtil.turnBroadcast(playerName));
        registry.sendToPlayer(playerId, GamePromptUtil.getMessage(phase));
        return coordinator.awaitReadyInput();
    }

    private GameAction buildPlayingAction(int playerId, ActionType actionType, String input) {
        if (actionType == ActionType.PASS_CARD) {
            return new GameAction(playerId, actionType, List.of());
        }

        PlayerState playerState = currentRoom.getPlayerById(playerId);
        if (playerState == null) {
            logServer("出牌失败：玩家不存在，playerId=" + playerId);
            return null;
        }

        try {
            List<Integer> cards = new ArrayList<>(CardUtil.stringToCards(input, playerState.getCards()));
            logPlayPreview(playerId, input, cards);
            return new GameAction(playerId, actionType, cards);
        } catch (IllegalArgumentException e) {
            registry.sendToPlayer(playerId, "输入无效：" + e.getMessage());
            logServer("玩家 " + playerId + " 出牌输入无效：" + e.getMessage());
            return null;
        }
    }

    private void handlePlayingResult(int playerId, String rawInput, GameAction action, GameResult gameResult) {
        if (gameResult == null) {
            logServer("出牌处理返回空，流程继续等待下一轮");
            return;
        }

        logGameResult(playerId, gameResult);

        PlayerState playerState = currentRoom.getPlayerById(playerId);
        if (playerState != null) {
            if (gameResult.isAccepted() || gameResult.isGameSettled()) {
                Collection<Integer> playedCards = action.getCards();
                String playerName = getPlayerName(playerId);
                String actionResult = action.getType() == ActionType.PASS_CARD ? "不出" : "出牌";
                String remainingCardsText = GamePromptUtil.remainingCardsBroadcast(remainingCardsSummaries());

                safelyLog(
                        () -> logActionRecord("PLAYING", playerId, playerName, rawInput, actionResult),
                        "记录出牌动作"
                );

                registry.broadcastExcept(
                        playerId,
                        GamePromptUtil.playedCardsBroadcast(playerName, CardUtil.cardsToString(playedCards), remainingCardsText)
                );
                registry.sendToPlayer(playerId, remainingCardsText);
            } else if (gameResult.isRejected()) {
                registry.sendToPlayer(playerId, gameResult.getMessage());
            }

            if (playerState.getCards().isEmpty()) {
                settledWinnerPlayerId = gameResult.getWinnerPlayerId();
                safelyLog(() -> finishCurrentSession(GameEndReason.NORMAL_SETTLEMENT, settledWinnerPlayerId), "结算收尾");
                for (Map.Entry<Integer, String> entry : gameResult.getPlayerMessages().entrySet()) {
                    registry.sendToPlayer(entry.getKey(), entry.getValue());
                }
            } else {
                registry.sendToPlayer(playerId, "你的手牌：\n" + CardUtil.cardsToString(playerState.getCards()));
                logPlayerCards(playerState, "玩家处理后手牌");
            }
        }

        logRoomState("出牌处理后");
    }

    private boolean handleReplayPhase() {
        Set<Integer> playerIds = Set.copyOf(registry.collectPlayerIds());
        if (playerIds.size() < 3) {
            safelyLog(() -> finishCurrentSession(GameEndReason.PLAYER_EXIT_AFTER_SETTLEMENT, settledWinnerPlayerId), "结算后退出收尾");
            registry.broadcast("有玩家退出，房间结束");
            shutdownRoom();
            return false;
        }

        coordinator.beginReplayVote(playerIds);
        registry.broadcast(GamePromptUtil.replayPrompt());
        Map<Integer, PlayerInput> replayVotes = coordinator.awaitReplayVotes();
        if (replayVotes.size() != playerIds.size()) {
            registry.broadcast("有玩家退出，房间结束");
            shutdownRoom();
            return false;
        }

        for (Map.Entry<Integer, PlayerInput> entry : replayVotes.entrySet()) {
            int playerId = entry.getKey();
            PlayerInput vote = entry.getValue();
            String playerName = getPlayerName(playerId);
            String actionResult = "1".equals(vote.message()) ? "继续下一把" : "退出";
            safelyLog(
                    () -> logActionRecord("SETTLE_VOTE", playerId, playerName, vote.message(), actionResult),
                    "记录结算投票"
            );
        }

        boolean allContinue = replayVotes.values().stream()
                .allMatch(vote -> "1".equals(vote.message()));
        if (!allContinue) {
            safelyLog(() -> finishCurrentSession(GameEndReason.PLAYER_EXIT_AFTER_SETTLEMENT, settledWinnerPlayerId), "结算后退出收尾");
            registry.broadcast("有玩家退出，房间结束");
            shutdownRoom();
            return false;
        }

        registry.broadcast("三位玩家都选择继续，开始下一把");
        currentRoom = gameFlow.startNewRoom(currentRoom);
        landlordState = currentRoom.getLandlordState();
        settledWinnerPlayerId = null;
        startSessionLogging();
        logRoomState("下一把房间创建完成");
        logServer("底牌已生成：" + CardUtil.cardsToString(currentRoom.getHoleCards()));
        sendOpeningHands(currentRoom);
        return true;
    }

    private boolean handleGameResult(Integer playerId, String rawInput, GameAction action) {
        GameResult gameResult = null;
        GamePhase phase = currentRoom.getCurrentPhase();

        if (phase == GamePhase.CALL_LANDLORD || phase == GamePhase.ROB_LANDLORD) {
            gameResult = gameFlow.handlePlayerAction(currentRoom, action);

            if (gameResult == null) {
                logServer("动作处理返回空，流程结束");
                return false;
            }

            broadcastResult(playerId, gameResult);
            if (gameResult.isAccepted()
                    || gameResult.isLandlordDecided()
                    || gameResult.isRedealRequired()) {
                String playerName = getPlayerName(playerId);
                String resultMessage = landlordActionResult(phase, action.getType());
                safelyLog(
                        () -> logActionRecord(phase.name(), playerId, playerName, rawInput, resultMessage),
                        "记录地主阶段动作"
                );
            }
            logGameResult(playerId, gameResult);
            logRoomState("叫抢地主处理后");
        }

        if (gameResult != null && gameResult.isLandlordDecided()) {
            safelyLog(() -> gameLogService.updateLandlordPlayerId(currentSessionId, currentRoom.getLandlordPlayerId()), "更新地主");
            registry.broadcast("地主已确定：玩家 " + currentRoom.getLandlordPlayerId());
            registry.broadcast("地主底牌：" + CardUtil.cardsToString(currentRoom.getHoleCards()));
            sendOpeningHands(currentRoom);
            logLandlordState("地主确认后");
        }

        if (gameResult != null && gameResult.isRedealRequired()) {
            currentRoom = gameFlow.reDeal(currentRoom);
            landlordState = currentRoom.getLandlordState();
            registry.broadcast(gameResult.getMessage());
            sendOpeningHands(currentRoom);
            logRoomState("重新发牌后");
            return true;
        }

        return true;
    }

    private void broadcastResult(Integer playerId, GameResult gameResult) {
        if (gameResult.isAccepted()) {
            registry.broadcast(gameResult.getMessage());

            PlayerSession session = registry.findByPlayerId(playerId);
            if (session != null) {
                registry.broadcastExcept(playerId, session.getPlayerName() + " " + gameResult.getMessage());
            }
        } else if (gameResult.isRejected()) {
            registry.sendToPlayer(playerId, gameResult.getMessage());
        }
    }

    private void sendOpeningHands(GameRoom room) {
        for (PlayerSession connection : registry.snapshot()) {
            PlayerState playerState = room.getPlayerById(connection.getPlayerId());
            if (playerState == null) {
                continue;
            }
            connection.send("你的手牌：\n" + CardUtil.cardsToString(playerState.getCards()));
            logPlayerCards(playerState, "发送手牌");
        }
    }

    private void logServer(String message) {
        System.out.println("[Server] " + message);
    }

    private void startSessionLogging() {
        currentSessionId = null;
        safelyLog(() -> currentSessionId = gameLogService.startSession(registry.collectPlayerNames()), "创建对局日志");
    }

    private void shutdownRoom() {
        registry.closeAll();
    }

    private void logActionRecord(String phase,
                                 int playerId,
                                 String playerName,
                                 String actionInput,
                                 String actionResult) {
        if (currentSessionId == null) {
            return;
        }
        gameLogService.appendAction(
                currentSessionId,
                phase,
                playerId,
                playerName,
                actionInput,
                actionResult,
                remainingCardsFor(1),
                remainingCardsFor(2),
                remainingCardsFor(3)
        );
    }

    private int remainingCardsFor(int playerId) {
        if (currentRoom == null) {
            return 0;
        }
        PlayerState playerState = currentRoom.getPlayerById(playerId);
        return playerState == null ? 0 : playerState.getCards().size();
    }

    private List<String> remainingCardsSummaries() {
        List<String> summaries = new ArrayList<>();
        for (PlayerSession session : registry.snapshot()) {
            summaries.add(session.getPlayerName() + " " + remainingCardsFor(session.getPlayerId()) + "张");
        }
        return summaries;
    }

    private void finishCurrentSession(GameEndReason endReason, Integer winnerPlayerId) {
        if (currentSessionId == null) {
            return;
        }
        Integer landlordPlayerId = currentRoom == null ? null : currentRoom.getLandlordPlayerId();
        WinnerSide winnerSide = null;
        if (winnerPlayerId != null && landlordPlayerId != null) {
            winnerSide = winnerPlayerId.equals(landlordPlayerId) ? WinnerSide.LANDLORD : WinnerSide.FARMER;
        }
        gameLogService.finishSession(currentSessionId, landlordPlayerId, winnerSide, winnerPlayerId, endReason);
    }

    private String landlordActionResult(GamePhase phase, ActionType actionType) {
        if (phase == GamePhase.CALL_LANDLORD) {
            return actionType == ActionType.CALL ? "叫地主" : "不叫地主";
        }
        if (phase == GamePhase.ROB_LANDLORD) {
            return actionType == ActionType.CALL ? "抢地主" : "不抢";
        }
        return "";
    }

    private void safelyLog(Runnable task, String scene) {
        try {
            task.run();
        } catch (Exception e) {
            logServer("对局日志写入失败：" + scene + "，" + e.getMessage());
        }
    }

    private String getPlayerName(int playerId) {
        PlayerSession session = registry.findByPlayerId(playerId);
        return session == null ? String.valueOf(playerId) : session.getPlayerName();
    }

    private void logTurnStart(Integer playerId, GamePhase phase) {
        System.out.println(GamePromptUtil.turnConsoleTitle(getPlayerName(playerId)));
        System.out.println("当前阶段 = " + phase);
        if (currentRoom != null) {
            System.out.println("地主玩家ID = " + currentRoom.getLandlordPlayerId());
        }
    }

    private void logGameResult(Integer playerId, GameResult gameResult) {
        System.out.println("==== 玩家" + playerId + " 动作处理结果 ====");
        System.out.println("是否成功 = " + gameResult.isSuccess());
        System.out.println("事件类型 = " + gameResult.getEventType());
        System.out.println("结果消息 = " + gameResult.getMessage());
        if (!gameResult.getPlayerMessages().isEmpty()) {
            System.out.println("玩家消息 = " + gameResult.getPlayerMessages());
        }
    }

    private void logPlayPreview(int playerId, String input, List<Integer> cards) {
        System.out.println("==== 玩家" + playerId + " 出牌预览 ====");
        System.out.println("原始输入 = " + input);
        System.out.println("解析后牌ID = " + cards);
        System.out.println("解析后牌面 = " + CardUtil.cardsToString(cards));
    }

    private void logRoomState(String title) {
        if (currentRoom == null) {
            logServer(title + "：currentRoom = null");
            return;
        }

        System.out.println("---- " + title + " ----");
        System.out.println("当前阶段 = " + currentRoom.getCurrentPhase());
        System.out.println("当前操作玩家ID = " + currentRoom.getCurrentPlayerId());
        System.out.println("地主玩家ID = " + currentRoom.getLandlordPlayerId());
        System.out.println("底牌 = " + CardUtil.cardsToString(currentRoom.getHoleCards()));

        if (landlordState != null) {
            System.out.println("首个叫地主玩家ID = " + landlordState.getFirstCallerId());
            System.out.println("当前地主候选人ID = " + landlordState.getLandlordCandidateId());
            System.out.println("不叫次数 = " + landlordState.getCallPassCount());
            System.out.println("叫地主阶段不叫玩家 = " + landlordState.getCallPassPlayerIds());
        }
        System.out.println();
    }

    private void logLandlordState(String title) {
        System.out.println("---- " + title + " ----");
        System.out.println("已确认地主ID = " + currentRoom.getLandlordPlayerId());
        System.out.println("首个叫地主玩家ID = " + landlordState.getFirstCallerId());
        System.out.println("当前地主候选人ID = " + landlordState.getLandlordCandidateId());
        System.out.println("不叫次数 = " + landlordState.getCallPassCount());
        System.out.println("底牌 = " + CardUtil.cardsToString(currentRoom.getHoleCards()));
        System.out.println();
    }

    private void logPlayerCards(PlayerState playerState, String title) {
        System.out.println("[Cards] " + title + "：玩家" + playerState.getPlayerId()
                + " = " + CardUtil.cardsToString(playerState.getCards()));
    }

    private static GameLogService createGameLogService() {
        try {
            return new GameLogService(new JdbcGameLogRepository());
        } catch (Exception e) {
            System.out.println("[Server] 对局日志初始化失败：" + e.getMessage());
            return new GameLogService(new NoOpGameLogRepository());
        }
    }
}
