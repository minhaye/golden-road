package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

public class AggressiveBehavior implements MonsterBehavior {
    @Override
    public void update(Monster monster, Player player, CollisionMap collisionMap) {
        if (player == null) {
            monster.setState(MonsterState.IDLE);
            return;
        }

        monster.moveToward(player.getX(), player.getY());
        monster.setState(MonsterState.MOVE);
    }
}