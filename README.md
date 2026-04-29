# 斗地主局域网对战

这是一个基于 Java Socket 的三人斗地主局域网对战项目。它的重点不是做一个单机规则 Demo，而是把一局斗地主拆成服务端权威状态、客户端输入输出、游戏阶段流转、牌型算法和数据库记录几块来实现。

服务端负责整局游戏的可信逻辑：玩家登录注册、并行认证、房间凑人、发牌、叫地主、抢地主、出牌校验、结算、下一把投票和日志落库。客户端只负责连接服务端、展示消息、读取控制台输入并发送回去。也就是说，客户端不保存真实游戏状态，不判断牌型，不访问数据库，所有会影响胜负的判断都在服务端完成。

一局游戏的主流程是：

```text
服务端启动
  -> 等待 3 个客户端连接并完成登录
  -> 洗牌发牌，留 3 张底牌
  -> 随机选择起始玩家叫地主
  -> 进入抢地主阶段，直到地主确定
  -> 地主拿底牌，从地主开始出牌
  -> 有玩家手牌出完后结算
  -> 3 个玩家同时选择是否下一把
  -> 全部继续则重新发牌，否则关闭房间
```

## 核心逻辑

### 游戏阶段流转

游戏状态集中在 `GameRoom` 里，包括当前阶段、当前操作玩家、地主玩家、三名玩家手牌、底牌、叫抢地主状态和出牌状态。

阶段枚举在 `GamePhase`：

- `WAITING`：等待状态。
- `DEALING`：发牌或重新发牌。
- `CALL_LANDLORD`：叫地主。
- `ROB_LANDLORD`：抢地主。
- `PLAYING`：出牌。
- `SETTLE`：结算。

`GameFlow` 是游戏主调度器。它不直接写网络代码，只接收 `GameAction`，再按当前阶段分发给不同处理器：

- `CallLandlordHandler`：处理叫地主和不叫。
- `RobLandlordHandler`：处理抢地主和不抢。
- `PlayingHandler`：处理出牌、不出和结算。

这样服务端网络层只管收输入和发消息，真正的规则推进留在 `game` 和 `rule` 里。

### 发牌算法

牌使用 `0` 到 `53` 的整数表示。普通牌按 `cardId / 4 + 1` 计算点数，小王和大王单独表示。

发牌在 `GameFlow.deal(...)`：

- 调用 `CardUtil.createShuffledDeck()` 生成并洗乱 54 张牌。
- 前 3 张作为底牌。
- 剩下 51 张按顺序轮流发给 3 个玩家。
- 每个玩家手牌用 `TreeSet<Integer>` 保存，天然保持有序，展示和规则判断都更稳定。

### 叫地主和抢地主

叫地主阶段由 `CallLandlordHandler` 处理。

服务端会随机选一个起始玩家。玩家可以叫地主或不叫：

- 有人叫地主后，进入抢地主阶段。
- 如果前两名玩家都不叫，第三名玩家叫地主，则直接成为地主。
- 如果三名玩家都不叫，服务端重新发牌。

抢地主阶段由 `RobLandlordHandler` 处理。

这里记录两个关键状态：

- `firstCallerId`：第一个叫地主的人。
- `landlordCandidateId`：当前地主候选人。

每次有人抢地主，就更新候选人。流程回到第一个叫地主的人时，候选人就成为地主。叫地主阶段已经选择不叫的人，在抢地主阶段会被自动当成不抢。

地主确认后，`GameFlow.confirmLandlord(...)` 会做三件事：

- 把房间阶段切到 `PLAYING`。
- 把 3 张底牌加入地主手牌。
- 把当前操作玩家设为地主。

### 出牌算法

出牌校验分两层。

第一层是 `PlayCardGroup.analyzeCards(...)`，负责识别这一组牌是什么牌型。它会先把牌转换成点数，再统计每个点数出现次数，然后判断牌型。

当前支持的牌型包括：

- 单牌
- 对子
- 三张
- 三带一
- 三带二
- 顺子
- 连对
- 飞机
- 飞机带单
- 飞机带对
- 四带两单
- 四带两对
- 炸弹
- 火箭

第二层是 `PlayingRuleChecker.checkPlay(...)`，负责判断当前出牌在这一局面下是否合法：

- 当前阶段必须是 `PLAYING`。
- 空牌表示不出，可以通过基础校验。
- 非空牌必须能识别出合法牌型。
- 如果桌面上没有上一手牌，可以直接出。
- 如果有上一手牌，则必须能压过上一手。

压牌规则里有两个特殊判断：

- 火箭最大，可以压非火箭的任何牌。
- 炸弹可以压非炸弹、非火箭的普通牌型。

普通牌型比较时，必须满足：

- 牌型相同。
- 牌数相同。
- 主点数更大。

主点数由 `PlayCardGroup` 提取。比如三带一比较三张的点数，飞机比较飞机本体最后一组三张的点数，四带二比较四张的点数。

### 出牌阶段推进

`PlayingHandler` 负责把出牌结果推进到下一状态。

玩家真正出牌时：

- 先检查牌型和压牌关系。
- 再从当前玩家手牌里移除这些牌。
- 更新 `lastPlayedCards` 和 `highestCardPlayerId`。
- 如果该玩家手牌为空，进入结算。
- 否则轮到下一名玩家。

玩家选择不出时：

- 如果当前玩家就是这一轮最大牌的玩家，不允许不出。
- 连续两个玩家不出后，回到最大牌玩家重新出牌。
- 新一轮开始时会清掉上一手牌和连续不出次数。

这个逻辑保证了斗地主里“没人压得过就回到出牌最大的人继续出”的规则。

## 服务端设计

服务端入口是 `server.net.Server`，实际启动逻辑在 `ServerApplication`。

服务端主要分成几块：

- `server.auth`：登录、注册、7 天内免密登录、数据库用户读写。
- `server.session`：管理已认证玩家连接、群发、单发、关闭连接。
- `server.flow`：协调当前轮到谁输入，以及结算后 3 人并行投票。
- `server.game`：把 Socket 输入转换成游戏动作，驱动 `GameFlow`。
- `server.log`：记录每局和每步动作日志。

服务端启动后会先等待玩家。每个新连接都会单独开认证线程，所以多个客户端可以同时登录，不会因为前一个玩家还在输密码就卡住后面的连接。

3 个玩家都认证完成后，服务端启动 `PlayerMessageListener` 监听每个客户端输入。客户端发来的每一行文本都会进入 `TurnInputCoordinator`：

- 如果当前是普通回合，只接受当前玩家的输入。
- 如果不是当前玩家，会返回“现在还没轮到你操作”。
- 如果当前是结算投票，会同时接受 3 个玩家的选择。
- 如果等待输入时玩家断线，服务端会唤醒主流程并结束房间。

`GameServerRunner` 是服务端对局驱动器。它负责：

- 给玩家发送手牌和当前阶段提示。
- 等待当前玩家输入。
- 把输入解析成 `ActionType`。
- 调用 `GameFlow.handlePlayerAction(...)`。
- 广播结果。
- 写入对局日志。
- 结算后处理下一把投票。

## 客户端设计

客户端入口有 4 个：

- `client.PlayerClient`
- `client.Player1`
- `client.Player2`
- `client.Player3`

`Player1`、`Player2`、`Player3` 是快捷入口，本质上还是启动同一套客户端逻辑。

客户端分成几块：

- `client.bootstrap`：组装客户端应用。
- `client.net`：Socket 连接、发送输入、读取服务端消息。
- `client.console`：读取控制台输入。
- `client.auth`：记录客户端是否已经完成认证。

客户端没有本地规则判断。比如玩家输入 `33`，客户端不会判断这是不是合法出牌，只会原样发给服务端。服务端会按玩家当前手牌自动匹配两张 3，再判断这次出牌是否合法。

服务端主动关闭连接时，客户端读线程会退出，整个客户端也会跟着结束，不会继续卡在控制台里。

## 数据库和日志

建表 SQL 在：

```text
D:\java\Game\game.sql
```

目前有三张表：

- `user`：用户表，保存用户名、密码、状态和最后登录时间。
- `game_session_log`：单局日志，保存一局的开始结束时间、玩家名、地主、赢家、结束原因。
- `game_action_log`：动作日志，保存每一步动作的阶段、玩家、原始输入、动作结果和三名玩家剩余手牌数。

日志现在保持简单，只记录复盘和排查最需要的信息，不保存完整手牌快照。

数据库连接配置在：

```text
D:\java\Game\doudizhu\src\main\resources\db.properties
```

默认配置如下，本机账号密码不一样时改这个文件即可：

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/doudizhu
db.username=root
db.password=200518
```

## 项目结构

```text
D:\java\Game
├── README.md
├── game.sql
└── doudizhu
    ├── pom.xml
    └── src
        ├── main/java
        │   ├── client
        │   ├── server
        │   ├── game
        │   ├── rule
        │   └── util
        ├── main/resources
        │   └── db.properties
        └── test/java
```

## 运行方式

需要 JDK 21、Maven 和 MySQL。

先导入数据库：

```powershell
mysql -u root -p < D:\java\Game\game.sql
```

编译并运行测试：

```powershell
cd D:\java\Game\doudizhu
mvn test
```

启动服务端：

```powershell
mvn exec:java -Dexec.mainClass="server.net.Server"
```

再分别启动三个客户端：

```powershell
mvn exec:java -Dexec.mainClass="client.Player1"
mvn exec:java -Dexec.mainClass="client.Player2"
mvn exec:java -Dexec.mainClass="client.Player3"
```

也可以直接在 IDE 里运行这些类的 `main` 方法。

## 输入方式

登录和注册阶段按服务端提示输入。

叫地主和抢地主阶段按提示输入对应选项。

出牌阶段：

- 输入点数表示出牌，不需要输入花色，比如 `33`、`3 3`、`3334`、`10JQKA`。
- 花色只用于服务端展示手牌，玩家输入时不用管。
- 输入 `PASS` 或直接回车表示不出。

## 测试覆盖

项目使用 JUnit 5。当前测试覆盖：

- 牌型识别和出牌规则。
- 叫地主、抢地主、重新发牌和地主确认。
- 出牌、不出、连续过牌和结算。
- 服务端登录注册、7 天内免密登录。
- 玩家连接注册、输入等待和并行结算投票。
- 对局日志服务。

运行：

```powershell
mvn test
```
