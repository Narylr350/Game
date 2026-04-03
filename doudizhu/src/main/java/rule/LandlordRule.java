package rule;

import game.GameRoom;
import game.PlayerState;

// Stage-one placeholder: only basic room/player validation is wired for now.
public class LandlordRule {
    //是否能叫地主
    public void canCallLandlord(GameRoom room, int playerId) {
        if (room == null) {
            throw new IllegalArgumentException("对局不能为空");
        }
        if (room.findPlayerById(playerId) == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
        for (PlayerState player : room.getPlayers()) {
            if (player.isLandlord()) {
                throw new IllegalArgumentException("还有地主");
            }
        }
    }

    /*
        3个玩家连接完成后，随机一个玩家作为首叫玩家。
        轮到玩家时，只能选 抢 或 不抢。
        选 抢：
        该玩家 score + 1
        如果该玩家分数到 2，立刻成为地主，抢地主流程结束
        如果只是到 1，继续轮到下一个玩家
        选 不抢：
        分数不变
        继续轮到下一个玩家
        如果 3 个人这一轮下来全都是 0 分：
        不用重新连接
        直接重新发牌，并重新随机首叫玩家
        如果没人到 2 分，但当前已经有人拿到过最高分：
        从“最后一次有人加分”开始算
        如果之后其余玩家都没有把分数抬得更高
        那么“最后一个达到当前最高分的人”直接当地主
     */
    //判断当前玩家是不是地主
    //true:表示地主阶段完成(抢到地主或者重开)
    //false:地主阶段未完成,继续
    public boolean resolveLandlordPhase(GameRoom room) {
        //重置地主状态
        for (PlayerState player : room.getPlayers()) {
            player.setLandlord(false);
        }
        //先到2分的直接判断为地主
        for (PlayerState player : room.getPlayers()) {
            if (player.getScore() == 2) {
                //将该玩家设为地主
                player.setLandlord(true);
                //将该玩家id设为地主id
                room.setLandlord(player.getPlayerId());
                //将底牌添加到地主手牌中
                player.addCards(room.getHoleCards());
                return true;
            }

            if (player.getScore() == 1 && room.getLastHighestScorerId().equals(player.getPlayerId())) {
                player.setLandlord(true);
            }
        }
        //不抢地主的计数器
        int count = 0;
        for (PlayerState player : room.getPlayers()) {
            if (player.getScore() == 0) {
                count++;
            }
        }
        //如果三人都不抢
        if (count == 3) {
            room.setGameStarted(false);
            room.setGameFinished(true);
            return true;
        }
        return false;
    }
}
