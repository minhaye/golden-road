package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

public class AirborneBehavior implements MonsterBehavior {
    private final float speedMultiplier;
    private final float altitudeRange;
    private final float verticalDrift;
    private final float phaseSpeed;
    private final float idleRadius;

    public AirborneBehavior(float leftBoundary, float rightBoundary, double speedMultiplier, float altitudeRange, float verticalDrift, float phaseSpeed, float idleRadius) {
        this.speedMultiplier = (float) speedMultiplier;
        this.altitudeRange = altitudeRange;
        this.verticalDrift = verticalDrift;
        this.phaseSpeed = phaseSpeed;
        this.idleRadius = idleRadius;
    }

    @Override
    public void update(Monster monster, Player player, CollisionMap collisionMap) {
        if (player != null) {
            float distance = monster.distanceTo(player.getX(), player.getY());
            if (distance <= monster.getDetectRange()) {

                float targetCenterX = monster.getPlayerCenterX(player);
                float targetCenterY = monster.getPlayerCenterY(player);
                monster.moveToward(
                        targetCenterX,
                        targetCenterY,
                        monster.getMoveSpeed() * speedMultiplier,
                        monster.getPlayerStopDistance(player)
                );
                monster.setState(MonsterState.MOVE);
                return;
            }
        }

        float t = (float) ((System.nanoTime() / 1_000_000.0) * phaseSpeed);
        float targetX = monster.getSpawnX() + (float) Math.cos(t) * altitudeRange;
        float targetY = monster.getSpawnY() + (float) Math.sin(t * 1.3f) * (verticalDrift + idleRadius);
        monster.moveToward(targetX, targetY, monster.getMoveSpeed() * (speedMultiplier * 0.75f));
        monster.setState(MonsterState.MOVE);
    }
}