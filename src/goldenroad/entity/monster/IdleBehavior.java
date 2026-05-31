package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

public class IdleBehavior implements MonsterBehavior {
    @Override
    public void update(Monster monster, Player player, CollisionMap collisionMap) {
        monster.setState(MonsterState.IDLE);
    }
}