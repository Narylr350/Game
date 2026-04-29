# doudizhu 分支改动说明

`main` 主线里，`game` 和 `rule` 已经有比较完整的斗地主核心逻辑：发牌、叫地主、抢地主、出牌、牌型判断、胜负判断。问题主要不在规则本身，而是在服务端和客户端接入层：服务端一边管 Socket，一边管登录注册，一边管当前轮到谁，一边又直接驱动游戏流程，很多变量和流程都挤在一起。能跑，但别人接手时很难知道“这段代码到底属于网络、登录、输入等待，还是游戏编排”。

这条分支的核心就是把这些职责拆清楚，尤其是 `server`。`game` 和 `rule` 只做了配合服务端的小改，不是重写核心规则。


1. 先看 `server.net.Server`，确认原来的启动入口还在。
2. 再看 `server.bootstrap.ServerApplication`，它是现在真正装配服务端的地方。
3. 然后看 `server.auth`，理解登录注册怎么从客户端挪到了服务端。
4. 再看 `server.session` 和 `server.flow`，理解“客户端发来的每一行输入”怎么被接住、判断、交给主流程。
5. 最后看 `server.game.GameServerRunner`，它负责把网络输入变成 `GameAction`，再调用 `game.flow.GameFlow`。
6. `game` 和 `rule` 放最后看，因为它们不是这次最大的改动点，主要是被服务端调用。

## server 改了什么

### 原来的问题

`main` 主线里的服务端思路更像一个大控制台程序：一个入口类里同时做很多事。

- 保存玩家连接列表。
- 接受客户端连接。
- 做登录、注册、数据库查询。
- 等待当前玩家输入。
- 拒绝不是当前玩家的输入。
- 拼 `GameAction`。
- 调 `GameFlow` 推进阶段。
- 给玩家广播消息。
- 一局结束后决定是否下一把。
- 打印服务端日志。

这些东西混在一起时，代码不一定马上错，但学习成本很高。比如你想改登录提示，可能会碰到 Socket；你想改“非当前玩家输入被拒绝”，又会碰到游戏阶段；你想改出牌解析，还可能被广播文案干扰。

这次拆分后，服务端按“这段代码在负责什么”分包。

### `server.net.Server`

现在它只保留兼容入口。

```java
public static void main(String[] args) {
    new ServerApplication().start(8888, 3);
}
```

它不再保存玩家列表，不再直接跑游戏，也不做登录。这样原来的启动方式不用变，但真正代码都转到 `ServerApplication`。

### `server.bootstrap.ServerApplication`

这是服务端的装配层。

它负责把几个模块组起来：

- `PlayerSessionRegistry`：保存已经认证成功的玩家连接。
- `TurnInputCoordinator`：协调当前该谁输入。
- `SocketAuthenticator`：处理登录注册问答。
- `GameServerRunner`：启动真正的斗地主对局。

它的主流程是：

1. 启动 `ServerSocket`。
2. 等待玩家连接。
3. 每来一个连接，开一个认证线程。
4. 认证成功后，把这个连接注册成正式玩家。
5. 满 3 个正式玩家后，启动每个玩家的消息监听线程。
6. 创建 `GameServerRunner`，进入对局。

这里比较重要的是“并行登录”。

以前如果服务端在 `accept` 后直接阻塞式登录，第一个玩家卡在输用户名或密码时，第二个、第三个玩家就只能等。现在每个 Socket 连接都会进入自己的认证线程，谁先登录成功谁先注册进房间。`CountDownLatch` 只负责等“已经认证成功的玩家数量达到 3”，不会强行要求玩家按顺序完成登录。

### `server.auth`

认证相关都放在这里，客户端不再查数据库。

主要类：

- `SocketAuthenticator`：管纯文本问答协议。它只关心从 `reader` 读一行、往 `writer` 发一行。
- `AuthSession`：管一次认证会话走到哪一步，比如选择登录/注册、输入用户名、输入密码。
- `AuthenticationService`：管真正的业务判断，比如用户是否存在、密码是否正确、注册名是否合法、7 天内是否可以免密。
- `UserRepository`：用户仓库接口。
- `JdbcUserRepository`：MySQL 实现，查 `user` 表、写注册用户、更新最近登录时间。
- `UserAccount`：服务端内部使用的用户模型。
- `AuthenticationResult`、`LoginDecision`、`AuthStepResult`：把认证结果表达清楚，避免用一堆字符串和布尔值猜状态。

登录流程大概是这样：

1. 服务端发：`请选择操作：1登录 2注册`
2. 客户端输入 `1`
3. 服务端发：`请输入用户名：`
4. 服务端根据用户名查库
5. 如果用户不存在，回到选择登录/注册
6. 如果用户存在并且 7 天内登录过，直接成功
7. 如果不能免密，继续发：`请输入密码：`
8. 密码正确后更新 `last_login_time`
9. `ServerApplication` 把这个 Socket 注册为正式玩家

注册流程也在服务端完成：

1. 客户端输入 `2`
2. 服务端收用户名
3. 服务端检查用户名格式和是否重复
4. 服务端收密码
5. 服务端检查密码格式
6. 写入数据库
7. 注册成功后直接进入游戏等待

这样改之后，客户端没有数据库账号，也没有密码校验逻辑。客户端只负责显示服务端提示，然后把玩家输入发回服务端。

### `server.session`

这个包只管“连接”。

主要类：

- `PlayerSession`：一个已经认证成功的玩家连接，里面有 `playerId`、`playerName`、`Socket`、输入流和输出流。
- `PlayerSessionRegistry`：连接池，负责注册玩家、查玩家、群发、单发、排除某个玩家广播、移除玩家、关闭所有连接。
- `PlayerMessageListener`：每个玩家一个监听线程，持续读取客户端输入。

这里有一个很关键的边界：`PlayerMessageListener` 不判断斗地主规则。

它只做三件事：

1. 从客户端读一行文本。
2. 把这行文本交给 `TurnInputCoordinator.submit(playerId, message)`。
3. 如果协调器拒绝，就把拒绝原因发回这个玩家。

比如玩家 2 在玩家 1 回合输入了 `33`，监听线程不会去解析 `33` 是不是一对 3，它只会问协调器：“现在能不能收玩家 2 的输入？”协调器返回拒绝后，玩家 2 会收到 `现在还没轮到你操作`。

### `server.flow`

这个包只管“当前应该收谁的输入”。

主要类：

- `TurnInputCoordinator`：当前回合输入协调器。
- `PlayerInput`：被接受的输入，包含玩家 ID、输入文本、当前阶段。
- `InputAcceptance`：提交输入后的接受/拒绝结果。

普通回合里，`GameServerRunner` 会先调用：

```java
coordinator.beginTurn(playerId, phase);
```

这表示现在只接受这个玩家在这个阶段的输入。然后主流程调用：

```java
coordinator.awaitReadyInput();
```

主流程会在这里等待，直到对应玩家真的输入了一行。

另一边，每个 `PlayerMessageListener` 都在各自线程里收客户端输入。收到后调用：

```java
coordinator.submit(session.getPlayerId(), message);
```

如果提交的是当前玩家，协调器保存这次输入并 `notifyAll()` 唤醒主流程。  
如果提交的是别的玩家，协调器直接拒绝。

一局结束后的“是否下一把”不是按顺序问的，所以这里还加了结算投票模式：

- `beginReplayVote(playerIds)`：进入投票模式。
- `awaitReplayVotes()`：等待三个人都投票。
- 三个人可以同时输入 `1` 或 `2`。
- 有人断开时，协调器会把它当成退出处理。

所以，`server.flow` 解决的是线程协作问题，不解决斗地主规则。

### `server.game.GameServerRunner`

这是服务端对局编排层，也是现在 server 里最重要的类。

它不负责 Socket 细节，也不负责查数据库。它拿到的是：

- 已经认证成功的玩家列表：`PlayerSessionRegistry`
- 当前输入协调器：`TurnInputCoordinator`
- 游戏核心流程：`GameFlow`
- 对局日志服务：`GameLogService`

它的主循环是：

1. 调 `gameFlow.startRoom(registry.collectPlayerNames())` 创建房间。
2. 给每个玩家发送手牌。
3. 看 `currentRoom.getCurrentPhase()` 当前阶段。
4. 看 `currentRoom.getCurrentPlayerId()` 当前轮到谁。
5. 广播 `系统：当前轮到玩家xxx操作`。
6. 单独提示当前玩家输入叫地主、抢地主或出牌。
7. 等 `TurnInputCoordinator` 返回当前玩家输入。
8. 把输入解析成 `ActionType`。
9. 如果是出牌阶段，用 `CardUtil.stringToCards(input, playerState.getCards())` 从玩家手牌里挑牌。
10. 构造 `GameAction`。
11. 调 `gameFlow.handlePlayerAction(currentRoom, action)`。
12. 根据 `GameResult` 决定广播、重发牌、确定地主、结算、下一把。

这里的重点是：`GameServerRunner` 是“服务端视角的流程编排”，不是规则本身。

比如玩家输入 `33`：

1. `PlayerMessageListener` 收到字符串 `33`。
2. `TurnInputCoordinator` 确认现在确实轮到这个玩家。
3. `GameServerRunner` 知道当前阶段是 `PLAYING`。
4. `ActionType.parseAction("33", PLAYING)` 判断这是出牌，不是 PASS。
5. `CardUtil.stringToCards("33", 当前玩家手牌)` 从手牌里找两张点数为 3 的牌。
6. `GameAction` 带着这两张牌进入 `GameFlow`。
7. `PlayingHandler` 和 `PlayingRuleChecker` 判断牌型、是否能压过上一手。
8. 成功后服务端广播“某某出了：xxx”，失败则只告诉这个玩家失败原因。

玩家不需要输入花色。花色只用于展示，真正解析时按点数从当前玩家手牌里取。

### `server.log`

这个包是后来加的简单对局日志。

主要类：

- `GameLogService`：日志业务入口。
- `GameLogRepository`：日志仓库接口。
- `JdbcGameLogRepository`：MySQL 实现。
- `NoOpGameLogRepository`：数据库不可用时的空实现，避免日志失败直接拖垮游戏。
- `GameSessionLog`：一局的概要。
- `GameActionLog`：一局里的每一步动作。
- `GameEndReason`：结束原因，比如正常结算、玩家结算后退出、掉线。
- `WinnerSide`：地主胜利或农民胜利。

日志现在只做简单记录，没有继续做复杂复盘 UI。记录粒度是：

- 一局什么时候开始。
- 三个玩家是谁。
- 地主是谁。
- 谁赢了。
- 每一步在哪个阶段。
- 哪个玩家输入了什么。
- 服务端识别出的动作结果是什么。
- 每步后 1、2、3 号玩家还剩几张牌。

这个设计是刻意精简的。它够查问题，也不会把服务端流程和复盘展示绑死。

## server 一条完整调用链

下面按一次真实游戏看代码怎么走。

### 连接和登录

1. `server.net.Server.main`
2. `ServerApplication.start(8888, 3)`
3. `ServerSocket.accept()`
4. 每个连接进入 `startAuthenticationThread`
5. `SocketAuthenticator.authenticate`
6. `AuthSession.handleInput`
7. `AuthenticationService.login/register`
8. `JdbcUserRepository` 访问数据库
9. 成功后 `PlayerSessionRegistry.registerAuthenticated`
10. 满 3 人后进入对局

这里任何一个玩家输慢了，都不会卡住其他玩家登录，因为每个连接都有自己的认证线程。

### 游戏输入

1. `GameServerRunner` 根据房间状态找到当前玩家。
2. `TurnInputCoordinator.beginTurn(playerId, phase)` 设置当前只收谁的输入。
3. `PlayerMessageListener` 收到所有客户端输入。
4. `TurnInputCoordinator.submit` 决定接受还是拒绝。
5. 接受后唤醒 `GameServerRunner.awaitReadyInput`。
6. `GameServerRunner` 把文本转成 `GameAction`。
7. `GameFlow.handlePlayerAction` 进入 `game` 核心流程。

所以如果要查“为什么某个输入没生效”，不要一上来查规则。先看它有没有被 `TurnInputCoordinator` 接受。

### 出牌成功

1. 玩家输入，比如 `33`。
2. `CardUtil.stringToCards` 根据玩家当前手牌解析成实际牌 ID。
3. `GameAction` 进入 `PlayingHandler`。
4. `PlayingRuleChecker` 判断牌型和压牌。
5. `GameResult.accepted` 或 `GameResult.gameSettled` 返回服务端。
6. `GameServerRunner` 广播给其他玩家，并把当前玩家的新手牌发回本人。
7. `GameLogService.appendAction` 写动作日志。

### 出牌失败

失败不会广播给所有人，只会告诉当前玩家。

常见失败有：

- 输入点数在手牌里找不到。
- 出牌牌型不合法。
- 当前牌压不过上一手。
- 轮到别人时抢着输入。

前两类通常在 `CardUtil` 或 `rule.play`。  
最后一类在 `TurnInputCoordinator`。

### 一局结束和下一把

1. 有玩家手牌为空。
2. `GameResult.gameSettled` 返回胜负信息。
3. `GameServerRunner` 写本局结束日志。
4. 房间阶段进入 `SETTLE`。
5. `TurnInputCoordinator.beginReplayVote` 同时等三个人选择。
6. 三个人都输入 `1`，调用 `gameFlow.startNewRoom(currentRoom)` 开下一把。
7. 只要有人输入 `2` 或断开，服务端广播房间结束并关闭连接。

这里也不是依次问，三个人可以同时投票。

## client 改了什么

客户端现在非常薄。

保留的入口：

- `client.PlayerClient`
- `client.Player1`
- `client.Player2`
- `client.Player3`

真正代码拆到：

- `client.bootstrap.ClientApplication`
- `client.net.ClientConnection`
- `client.net.ServerMessageReader`
- `client.console.ConsoleInputLoop`
- `client.auth.ClientAuthState`

客户端现在只做两件事：

1. 把服务端发来的文本打印出来。
2. 把玩家在控制台输入的文本发给服务端。

它不再判断用户名是否存在，不再校验密码，不再访问 MySQL，也不判断斗地主规则。

这样做的原因很简单：局域网小游戏里，客户端可以随便改，不能信。真正可信的是服务端。玩家有没有这张牌、现在该不该他出、能不能压过上一手，都应该由服务端说了算。

## game 改了什么

`game` 没有重写主流程，只做了服务端编排需要的小改。

主要变化：

- `GameFlow` 把重复的发牌建房逻辑抽成 `createDealtRoom`。
- `GameFlow.startNewRoom` 用旧房间里的玩家名字重新开局。
- `GameResult` 增加了更清楚的语义方法，比如 `isAccepted`、`isRejected`、`isLandlordDecided`、`isRedealRequired`、`isGameSettled`。
- `GameResult` 增加 `winnerPlayerId`，服务端写日志和判断地主/农民胜负时不用猜。
- `GameAction` 做了不可变和空牌保护，避免外部传进来的牌集合后面被改。
- `PlayingHandler` 拆出了一些小方法，让出牌阶段更容易读。

这些改动都是为了让服务端少写硬编码判断，比如以前服务端可能要判断 `eventType == XXX`，现在可以直接看 `gameResult.isGameSettled()`。这不是改变规则，而是把结果表达得更清楚。

## rule 改了什么

`rule` 仍然是规则算法层，不碰 Socket，不碰数据库。

主要变化：

- `PlayCardAnalyzer` 专门负责识别牌型。
- `PlayCardGroup` 更像一个牌型识别结果，里面保存牌型、主值、牌数量等信息。
- `PlayingRuleChecker` 专门判断当前出牌是否合法、能不能压过上一手。
- `LandlordRuleChecker` 补了叫地主、抢地主阶段的规则判断。

拆完之后，出牌判断的阅读路径更清楚：

1. 先用 `PlayCardAnalyzer` 看这组牌是什么牌型。
2. 再用 `PlayingRuleChecker` 看当前能不能出。
3. 如果是叫抢地主阶段，就看 `LandlordRuleChecker`。

以后如果规则错了，优先查 `rule`。  
如果规则没错但服务端收错了输入，优先查 `server.flow` 或 `server.game`。

## util 改了什么

`util` 只放真正通用的工具。

现在主要有：

- `CardUtil`：建牌、洗牌、牌面展示、把玩家输入解析成手牌里的实际牌。
- `AuthJdbcUtil`：读取数据库配置、创建连接、关闭 JDBC 资源。
- `CredentialPolicy`：用户名和密码格式规则。
- `GamePromptUtil`：服务端和客户端要显示的固定提示文案。

规则算法没有放进 `util`，因为规则不是工具。  
流程状态也没有放进 `util`，因为它们属于 `game`。

## 新加的功能

这条分支除了拆代码，也补了一些功能。

- 登录注册全部移到服务端。
- 多个客户端可以并行登录。
- 登录成功后会更新最近登录时间。
- 7 天内再次登录可以免密。
- 支持一局结束后三人同时选择是否下一把。
- 有人不继续或断开，整桌结束。
- 服务端写简单对局日志到数据库。
- 数据库连接配置放到 `src/main/resources/db.properties`。
- MySQL Maven 依赖换成 `com.mysql:mysql-connector-j`。
- 玩家出牌输入支持 `33`、`3334`、`10JQKA`、`小王大王` 这种点数写法，不要求输入花色。

## 数据库变化

SQL 写在项目根目录的 `game.sql`。

现在主要涉及三类表：

- `user`：用户登录注册。
- `game_session_log`：一局的概要日志。
- `game_action_log`：一局里的动作日志。

`user` 表增加了 `last_login_time`，用于 7 天免密登录。  
如果旧库里没有这个字段，`JdbcUserRepository` 会尝试自动补字段。

日志表只存简单字段，没有存完整手牌快照，也没有做复盘解析。之前想做网页复盘，但那个功能已经撤掉了，所以现在日志目标很明确：能查一局谁玩了、谁赢了、每一步是谁输入了什么。

## 怎么理解这次重构

可以把现在的项目分成四层。

第一层是客户端。

客户端只负责输入输出。它像一个远程控制台，不决定规则。

第二层是服务端接入层。

`server.auth` 管身份，`server.session` 管连接，`server.flow` 管输入等待，`server.game` 管对局编排，`server.log` 管日志。它们合起来让服务端成为可信中心。

第三层是游戏流程层。

`game` 管一局游戏的状态：当前阶段、当前玩家、地主是谁、玩家手牌、底牌、上一手牌、结算结果。

第四层是规则算法层。

`rule` 管能不能叫、能不能抢、这是什么牌型、这手牌能不能出。

最重要的是不要把这几层混着看。

如果你想改登录提示，看 `server.auth`。  
如果你想改“没轮到你不能输入”，看 `server.flow`。  
如果你想改广播给谁，看 `server.session` 和 `server.game`。  
如果你想改出牌解析，看 `CardUtil`。  
如果你想改牌型判断，看 `rule.play`。  
如果你想改阶段推进，看 `game.flow` 和 `game.handler`。

## 测试覆盖了什么

这条分支补了不少测试，目的不是追求数量，而是让重构后别靠手动开三个客户端碰运气。

覆盖点大概是：

- 登录成功、注册成功、用户不存在、密码错误、重复注册。
- 7 天免密登录。
- 当前玩家输入会被接受。
- 非当前玩家输入会被拒绝。
- 主流程能被输入唤醒。
- 结算投票可以等三个人一起输入。
- 玩家断开时能唤醒等待中的流程。
- 出牌输入解析，比如 `33` 不需要花色。
- 规则判断，比如牌型识别、压牌、叫抢地主。
- 对局日志服务和 JDBC 仓库。
- `GameResult`、`GameAction` 这些模型对象的小改没有破坏原有语义。

目前完整测试可以用：

```bash
mvn test
```

## 这次没有做什么

有些东西故意没做。

- 没有把文本协议升级成对象协议。
- 没有把客户端改成图形界面。
- 没有继续做网页复盘。
- 没有重写 `GameFlow` 主流程。
- 没有大规模重拆 `game` 包。
- 没有把规则算法塞进服务端。
- 没有把所有类都硬塞进 `util`。

这条分支的目标不是把项目改成很复杂的架构，而是让三个人写出来的代码能接起来、能读懂、能继续改。
