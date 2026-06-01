package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

public class PatrolBehavior implements MonsterBehavior {
    private final float leftBoundary;
    private final float rightBoundary;
    private final float patrolSpeedMultiplier;

    public PatrolBehavior(float leftBoundary, float rightBoundary, double patrolSpeedMultiplier) {
        this.leftBoundary = leftBoundary;
        this.rightBoundary = rightBoundary;
        this.patrolSpeedMultiplier = (float) patrolSpeedMultiplier;
    }

    @Override
    public void update(Monster monster, Player player, CollisionMap collisionMap) {
        if (player != null && monster.canDetectPlayer(player)) {
            monster.moveHorizontallyToward(player.getX());
            monster.setState(MonsterState.MOVE);
            return;
        }

        if (monster.getX() <= leftBoundary) {
            monster.setDirection(Direction.RIGHT);
        } else if (monster.getX() >= rightBoundary) {
            monster.setDirection(Direction.LEFT);
        }

        float step = monster.getMoveSpeed() * patrolSpeedMultiplier;
        if (monster.getDirection() == Direction.LEFT) {
            monster.setPosition(monster.getX() - step, monster.getY());
        } else {
            monster.setPosition(monster.getX() + step, monster.getY());
        }
        monster.setState(MonsterState.MOVE);
    }
}