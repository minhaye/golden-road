package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

public class PatrolBehavior implements MonsterBehavior {
    private static final int DEFAULT_IDLE_TICKS = 30;
    private static final float BORDER_MARGIN = 0.5f;

    private final float leftBoundary;
    private final float rightBoundary;
    private final float patrolSpeedMultiplier;
    private final int idleTicks;
    private int idleTicksRemaining;

    public PatrolBehavior(float leftBoundary, float rightBoundary, double patrolSpeedMultiplier) {
        this(leftBoundary, rightBoundary, patrolSpeedMultiplier, DEFAULT_IDLE_TICKS);
    }

    public PatrolBehavior(float leftBoundary, float rightBoundary, double patrolSpeedMultiplier, int idleTicks) {
        this.leftBoundary = Math.min(leftBoundary, rightBoundary);
        this.rightBoundary = Math.max(leftBoundary, rightBoundary);
        this.patrolSpeedMultiplier = (float) patrolSpeedMultiplier;
        this.idleTicks = Math.max(0, idleTicks);
        this.idleTicksRemaining = 0;
    }

    @Override
    public void update(Monster monster, Player player, CollisionMap collisionMap) {
        if (monster.isActionState()) {
            return;
        }

        if (player != null && monster.canDetectPlayer(player)) {
            monster.setAiState(MonsterAiState.CHASE);
            monster.moveHorizontallyToward(player.getX());
            monster.setState(MonsterState.MOVE);
            return;
        }

        if (idleTicksRemaining > 0) {
            idleTicksRemaining--;
            monster.setAiState(MonsterAiState.IDLE);
            monster.setState(MonsterState.IDLE);
            return;
        }

        float x = monster.getX();
        float speed = monster.getMoveSpeed() * patrolSpeedMultiplier;
        float rightEdgeX = (float) (rightBoundary - monster.getWidth());
        float safeRightBoundary = Math.max(leftBoundary, rightEdgeX);

        if (monster.getDirection() == Direction.LEFT && x <= leftBoundary + BORDER_MARGIN) {
            monster.setPosition(leftBoundary, monster.getY());
            monster.setDirection(Direction.RIGHT);
            idleTicksRemaining = idleTicks;
            monster.setAiState(MonsterAiState.IDLE);
            monster.setState(MonsterState.IDLE);
            return;
        }

        if (monster.getDirection() == Direction.RIGHT && x >= safeRightBoundary - BORDER_MARGIN) {
            monster.setPosition(safeRightBoundary, monster.getY());
            monster.setDirection(Direction.LEFT);
            idleTicksRemaining = idleTicks;
            monster.setAiState(MonsterAiState.IDLE);
            monster.setState(MonsterState.IDLE);
            return;
        }

        float nextX = monster.getDirection() == Direction.LEFT ? x - speed : x + speed;
        nextX = Math.max(leftBoundary, Math.min(nextX, safeRightBoundary));

        if (collisionMap != null
                && collisionMap.isLoaded()
                && !collisionMap.canStandAt(nextX, monster.getY(), monster.getWidth(), monster.getHeight())) {
            monster.setDirection(monster.getDirection() == Direction.LEFT ? Direction.RIGHT : Direction.LEFT);
            idleTicksRemaining = idleTicks;
            monster.setAiState(MonsterAiState.IDLE);
            monster.setState(MonsterState.IDLE);
            return;
        }

        if (monster.getDirection() == Direction.LEFT) {
            monster.setPosition(nextX, monster.getY());
        } else {
            monster.setPosition(nextX, monster.getY());
        }
        monster.setAiState(MonsterAiState.PATROL);
        monster.setState(MonsterState.MOVE);
    }
}
