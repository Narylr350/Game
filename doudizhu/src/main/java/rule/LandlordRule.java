package rule;

import game.GameRoom;
import game.PlayerState;

import java.util.Random;
import java.util.Scanner;

// Stage-one placeholder: only basic room/player validation is wired for now.
public class LandlordRule {
    public void canCallLandlord(GameRoom room, int playerId) {
        if (room == null) {
            throw new IllegalArgumentException("对局不能为空");
        }
        if (room.findPlayerById(playerId) == null) {
            throw new IllegalArgumentException("玩家不存在");
        }
    }

    public void callLandlord(GameRoom room, int playerId) {
        canCallLandlord(room, playerId);
        System.out.println("抢地主?");
        String input = new Scanner(System.in).nextLine();
        Random random = new Random();
        int firstPlayerId = random.nextInt(0,2);
        if ("抢地主".equals(input)) {
            PlayerState playerById = room.findPlayerById(firstPlayerId);
            playerById.addScore();
        }

    }
}
