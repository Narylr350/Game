# 斗地主项目协作方案

## 1. 当前项目现状

目前项目的目录已经开始往分层方向走了，这一步是对的。

现有结构里已经有：

- `server/net`
- `server/service`
- `server/model`
- `server/util`

但现在的问题是“目录分了，职责还没完全分开”。

当前主要问题：

- `server/net/Server.java` 还在直接驱动发牌流程。
- `server/service/GameService.java` 还依赖 `client.Player`。
- `server/model` 下的类已经建出来了，但还是空壳。
- `server/util/CardUtil.java` 还是空的，牌面转换逻辑还没迁进去。
- `client/Player1.java`、`client/Player2.java`、`client/Player3.java` 还是重复客户端。
- 连接状态和游戏状态还混在一起。

## 2. 分层目标

项目应该按职责拆，而不是按“先写哪里方便”来堆代码。

### `server/net`

职责：

- 管理 socket 连接
- 接收客户端消息
- 向客户端发送消息
- 把网络输入转成 service 调用

适合放这里的类：

- `Server`
- `PlayerConnection`
- 后续可以增加 `ClientHandler`、`MessageDispatcher`

不应该放这里的内容：

- 发牌算法
- 抢地主逻辑
- 出牌合法性判断
- 牌型识别

### `server/service`

职责：

- 控制游戏流程
- 开局
- 发牌
- 抢地主阶段
- 出牌阶段
- 结算阶段

适合放这里的类：

- `GameService`
- `GameSessionManager` 或 `GameCoordinator`

不应该放这里的内容：

- `client.Player` 依赖
- socket 操作

### `server/model`

职责：

- 纯数据模型
- 不做 IO
- 不打印控制台
- 不依赖 socket

适合放这里的类：

- `GameSession`
- `DealResult`
- `PlayerHand`
- `PlayerState`
- 后续可以增加 `PlayAction`、`PlayResult`

### `server/util`

职责：

- 放通用工具逻辑
- 牌 ID 转显示文本
- 手牌格式化
- 牌排序辅助

适合放这里的类：

- `CardUtil`
- 后续可以增加 `DeckFactory`

## 3. 建议重命名

### 现在就该改的

- `GameService.Licensing(...)` 改成 `deal(...)` 或 `dealCards(...)`
- `GameManager` 改成 `GameSessionManager` 或 `GameCoordinator`

### 可以保留但要收住职责的

- `Server`
- `PlayerConnection`
- `GameService`
- `CardUtil`

### 逐步替换掉的

- 服务端不要继续依赖 `client.Player`
- 改用 `server/model/PlayerHand` 和 `server/model/PlayerState`
- `Player1`、`Player2`、`Player3` 最终合成一个客户端入口，比如 `GameClient`

## 4. 什么要抽出去

### 从 `GameService` 抽出去

下面这些应该迁到 `CardUtil` 或专门的辅助类里：

- 牌面映射
- 牌转字符串
- 打印牌
- 牌堆构建辅助逻辑

### 从 `Server` 抽出去

下面这些应该迁到 `GameService` 和 `GameSession`：

- 开局流程控制
- 对局状态保存
- 抢地主流程
- 轮次推进
- 结算流程

### 从服务端对客户端模型的依赖里抽出去

`server/service/GameService.java` 不应该继续依赖 `client.Player`。

原因：

- 客户端模型不是服务端领域模型
- 这样会让协作边界不清楚
- 核心逻辑开发和网络开发会互相影响

## 5. 什么不适合继续放在现在的位置

### `server/net/Server.java`

这里不适合长期保留的内容：

- 业务状态管理
- 发牌细节
- 抢地主规则
- 出牌合法性判断

`Server` 应该只负责：

- 接收连接
- 创建玩家连接对象
- 调 service
- 给客户端发消息

### `server/service/GameService.java`

这里不适合继续保留的内容：

- `client.Player` 依赖
- 直接控制台输出
- 工具逻辑和业务逻辑混写

### `server/net/PlayerConnection.java`

它适合只表示“连接态”。

不适合继续往里面堆：

- 完整游戏规则状态
- 抢地主状态
- 全局轮次状态

这些更应该放进 `GameSession` 和 `PlayerState`。

### `client/Player1.java`、`client/Player2.java`、`client/Player3.java`

这三个类现在本质上是重复的客户端入口。

不适合继续保持这种结构，因为：

- 改一处要改三处
- 修 bug 要重复三次
- 协议一变要同步改多个文件

应该收敛成一个客户端入口类。

## 6. 三人分工建议

### 开发者 A：核心逻辑

负责：

- `server/model`
- 对局状态设计
- 开局流程
- 发牌结果流转
- 游戏生命周期数据

主要产出：

- `GameSession`
- `DealResult`
- `PlayerHand`
- `PlayerState`
- `GameService.startGame(...)`
- `GameService.deal(...)`

### 开发者 B：网络层

负责：

- `server/net`
- 客户端和服务端通信
- 连接生命周期
- 请求解析
- 响应发送
- 客户端入口整理

主要产出：

- `Server`
- `PlayerConnection`
- 后续的 `ClientHandler`
- 统一客户端入口

### 开发者 C：规则和玩法

负责：

- 抢地主规则
- 牌型识别
- 出牌合法性
- 特殊组合判断
- 轮次规则处理

建议新增包：

- `server/rule`

后续可以放的类：

- `LandlordRule`
- `CardTypeRule`
- `PlayRuleChecker`
- `RuleEngine`

## 7. 三人协作边界规则

为了避免三个人互相冲突，应该先把接口定住。

建议先固定这些 service 层接口：

```java
GameSession startGame(List<String> playerNames);
DealResult deal(List<String> playerNames);
boolean callLandlord(GameSession session, int playerId, boolean call);
PlayResult playCards(GameSession session, int playerId, List<Integer> cards);
String cardsToString(Collection<Integer> cards);
```

协作规则：

- 网络层只调用 service 接口
- 核心逻辑层定义 session 和状态流转
- 规则层只处理规则判断和合法性判断
- 除了网络层，不要碰 socket
- 除了 service/model 层，不要管理游戏状态

## 8. 推荐目录结构

```text
src/main/java
├─ client
│  ├─ GameClient.java
│  └─ ClientMessageHandler.java
├─ server
│  ├─ net
│  │  ├─ Server.java
│  │  ├─ PlayerConnection.java
│  │  └─ ClientHandler.java
│  ├─ service
│  │  ├─ GameService.java
│  │  └─ GameSessionManager.java
│  ├─ rule
│  │  ├─ LandlordRule.java
│  │  ├─ CardTypeRule.java
│  │  └─ PlayRuleChecker.java
│  ├─ model
│  │  ├─ GameSession.java
│  │  ├─ DealResult.java
│  │  ├─ PlayerHand.java
│  │  ├─ PlayerState.java
│  │  ├─ PlayAction.java
│  │  └─ PlayResult.java
│  └─ util
│     ├─ CardUtil.java
│     └─ DeckFactory.java
```

## 9. 推荐开发顺序

### 第一阶段

- 停止在服务端使用 `client.Player`
- 填充 `DealResult`
- 填充 `PlayerHand`
- 填充 `GameSession`

### 第二阶段

- 把牌转换逻辑迁到 `CardUtil`
- 把 `Licensing(...)` 改名为 `deal(...)`
- 让 `Server` 通过 service 调用，不直接碰发牌细节

### 第三阶段

- 合并 `Player1/2/3` 为一个客户端入口
- 定义请求和响应格式
- 整理连接流程

### 第四阶段

- 新建 `server/rule`
- 实现抢地主规则
- 实现出牌合法性规则
- 实现轮次推进和结算

## 10. 最终效果

最终应该达到这个效果：

- `Server` 只处理通信
- `GameService` 只处理游戏流程
- `GameSession` 成为整局状态的唯一来源
- 规则逻辑独立放在 `rule` 包
- 工具逻辑从 service 里抽出去
- 三个人可以并行开发，互相修改重叠最少

这套方案很适合你们现在这个练手协作项目。

## 11. 可直接开干的任务拆分表

这一部分按三个人当前就能并行推进的方式来拆，尽量减少互相等待。

### 开发者 A：核心逻辑

当前阶段目标：

- 把服务端游戏状态模型补全
- 让 `GameService` 不再依赖 `client.Player`
- 打通“开局 -> 发牌 -> 生成对局状态”这条主链路

第一批任务：

- 完成 `server/model/PlayerHand`
- 完成 `server/model/DealResult`
- 完成 `server/model/PlayerState`
- 完成 `server/model/GameSession`
- 重构 `server/service/GameService`

建议具体内容：

- `PlayerHand`
  - `playerId`
  - `playerName`
  - `TreeSet<Integer> cards`
- `DealResult`
  - `List<PlayerHand> playerHands`
  - `TreeSet<Integer> holeCards`
- `PlayerState`
  - `playerId`
  - `playerName`
  - `TreeSet<Integer> cards`
  - `boolean landlord`
  - `boolean online`
- `GameSession`
  - `List<PlayerState> players`
  - `TreeSet<Integer> holeCards`
  - `Integer landlordPlayerId`
  - `Integer currentTurnPlayerId`
  - `boolean gameStarted`
  - `boolean gameFinished`

`GameService` 第一阶段建议先提供：

```java
DealResult deal(List<String> playerNames);
GameSession startGame(List<String> playerNames);
```

当前交付标准：

- 服务端不再引用 `client.Player`
- `GameService` 能根据 3 个玩家名返回发牌结果
- `GameService.startGame(...)` 能返回初始化后的 `GameSession`

### 开发者 B：网络层

当前阶段目标：

- 整理服务端连接流程
- 让 `Server` 调用 service 而不是自己处理业务
- 收敛客户端重复代码

第一批任务：

- 重构 `server/net/Server`
- 保留并整理 `server/net/PlayerConnection`
- 合并 `client/Player1`、`Player2`、`Player3`
- 约定客户端首次连接消息格式

建议具体内容：

- `Server`
  - 接受 3 个连接
  - 读取纯名字
  - 调 `GameService.startGame(...)`
  - 按 `GameSession` 给每个玩家发手牌
- `PlayerConnection`
  - 保留连接信息
  - 不继续堆业务逻辑
- 客户端
  - 合并成 `GameClient`
  - 输入名字
  - 连接服务器
  - 第一句只发名字，不要发“xxx 已连接”

当前交付标准：

- 服务端能正常接 3 个玩家
- 客户端首包协议统一
- `Server` 不再自己写发牌细节
- 客户端不再维护三份重复代码

### 开发者 C：规则和玩法

当前阶段目标：

- 先把斗地主规则模块的骨架搭出来
- 不等网络和核心层完全完成，也能先写规则接口和测试数据

第一批任务：

- 新建 `server/rule`
- 设计牌型枚举或牌型判断结果对象
- 设计抢地主规则接口
- 设计出牌合法性接口

建议具体内容：

- `CardTypeRule`
  - 单张
  - 对子
  - 三张
  - 顺子
  - 连对
  - 飞机
  - 炸弹
  - 王炸
- `LandlordRule`
  - 是否允许叫地主
  - 叫地主轮转逻辑
- `PlayRuleChecker`
  - 当前出牌是否合法
  - 是否能压过上一手

当前交付标准：

- 先把规则接口定义好
- 至少完成基础牌型识别的框架
- 能独立写单元测试，不依赖 socket

## 12. 每个人的文件边界

为了避免 merge 冲突，建议先把“谁主要修改哪些文件”约定清楚。

### 开发者 A 主要改

- `src/main/java/server/model/*`
- `src/main/java/server/service/GameService.java`
- `src/main/java/server/service/GameSessionManager.java` 或 `GameCoordinator.java`

### 开发者 B 主要改

- `src/main/java/server/net/*`
- `src/main/java/client/*`

### 开发者 C 主要改

- `src/main/java/server/rule/*`
- 后续少量配合改 `server/model` 中与规则相关的结果对象

共同约定：

- 不随便跨边界大改别人主负责目录
- 如果要改公共模型，先同步字段设计
- 先定接口，再写实现

## 13. 第一周建议排期

如果你们是练手合作项目，第一周建议只追求“能跑通开局”，不要一口气做完整斗地主。

### 第 1 天

- A：补模型类字段
- B：整理 `Server` 和客户端首包协议
- C：设计规则模块类名和接口

### 第 2 天

- A：实现 `deal(...)` 和 `startGame(...)`
- B：让服务端成功把名字收齐并调用 service
- C：完成牌型枚举和基础判断框架

### 第 3 天

- A：完善 `GameSession`
- B：客户端显示自己的手牌
- C：补单张、对子、三张、炸弹判断

### 第 4 天

- A：配合 B 调整 session 输出结构
- B：整理消息收发结构
- C：补顺子、连对、飞机判断框架

### 第 5 天

- 三人联调
- 跑通完整流程：
  - 3 人连接
  - 服务端开局
  - 发牌
  - 客户端看到各自手牌

## 14. 当前最小里程碑

先不要把目标定成“完整斗地主上线”，先定成下面这个最小里程碑：

- 只有一个服务端入口
- 只有一个客户端入口
- 3 个客户端能连接
- 服务端能创建 `GameSession`
- 服务端能完成发牌
- 每个客户端能收到自己的牌

只要这个里程碑完成，后面再叠抢地主和出牌规则就稳很多。

## 15. 下一步最推荐先做什么

如果按优先级排，最推荐先做这 3 件事：

1. 开发者 A 先把 `DealResult`、`PlayerHand`、`GameSession` 填完整。
2. 开发者 B 先把客户端三份代码合成一个，并统一首包为“纯名字”。
3. 开发者 C 先把 `server/rule` 骨架和牌型判断接口搭起来。

这样三个人可以马上并行，不会都卡在一个文件上。
