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

        float targetCenterX = monster.getPlayerCenterX(player);
        float targetCenterY = monster.getPlayerCenterY(player);
        monster.moveToward(targetCenterX, targetCenterY, monster.getMoveSpeed(), monster.getPlayerStopDistance(player));

        monster.setState(MonsterState.MOVE);
    }
}