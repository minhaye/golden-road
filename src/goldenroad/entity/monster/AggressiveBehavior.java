package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

public class AggressiveBehavior implements MonsterBehavior {
    @Override
    public void update(Monster monster, Player player, CollisionMap collisionMap) {
        if (monster.isActionState()) {
            return;
        }

        if (player == null) {
            monster.setAiState(MonsterAiState.IDLE);
            monster.setState(MonsterState.IDLE);
            return;
        }

        float targetCenterX = monster.getPlayerCenterX(player);
        float targetCenterY = monster.getPlayerCenterY(player);
        monster.setAiState(MonsterAiState.CHASE);
        monster.moveToward(targetCenterX, targetCenterY, monster.getMoveSpeed(), monster.getPlayerStopDistance(player));

        monster.setState(MonsterState.MOVE);
    }
}
