package goldenroad.entity.monster;

import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionMap;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

final class MonsterMotion {
    private static final int TILE_SIZE = 16;
    private static final double GRAVITY = 0.7;
    private static final double MAX_FALL_SPEED = 9.0;
    private static final double RETREAT_SPEED = 2.6;
    private static final int DODGE_DURATION_FRAMES = 12;
    private static final int DODGE_COOLDOWN_FRAMES = 80;
    private static final int DODGE_GROW_X = 72;
    private static final int DODGE_GROW_Y = 18;
    private static final double LOW_HEALTH_RETREAT_SPEED_MULTIPLIER = 1.15;

    private final Rectangle bounds;

    private double x;
    private double y;
    private double velocityY = 0;
    private boolean onGround = true;
    private double spawnX;
    private double spawnY;
    private double patrolTargetX;
    private int patrolDirection = 1;
    private double facingDirection = 1;
    private int dodgeCooldownTimer = 0;
    private int dodgeTimer = 0;
    private double dodgeDirection = 0;
    private boolean placementResolved = false;
    private int resolvedMapWidth = -1;
    private int resolvedMapHeight = -1;

    MonsterMotion(int x, int y, int width, int height, double patrolRange) {
        this.bounds = new Rectangle(x, y, width, height);
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.patrolTargetX = x + patrolRange;
    }

    boolean updateDodgeStep(CollisionMap map) {
        if (dodgeTimer <= 0) {
            return false;
        }

        dodgeTimer--;
        moveHorizontal(dodgeDirection * RETREAT_SPEED * LOW_HEALTH_RETREAT_SPEED_MULTIPLIER, map, true);
        applyVerticalPhysics(map);
        updateBounds();
        return true;
    }

    void updateTimers() {
        if (dodgeCooldownTimer > 0) {
            dodgeCooldownTimer--;
        }
    }

    void refreshGroundState(CollisionMap map) {
        boolean standing = map.canStandAt(x, y, bounds.width, bounds.height);
        if (standing && velocityY >= 0) {
            onGround = true;
            velocityY = 0;
        } else if (!standing) {
            onGround = false;
        }
    }

    void resolvePlacement(CollisionMap map) {
        if (map == null || !map.isLoaded()) {
            placementResolved = true;
            return;
        }

        if (resolvedMapWidth != map.getWidth() || resolvedMapHeight != map.getHeight()) {
            placementResolved = false;
            resolvedMapWidth = map.getWidth();
            resolvedMapHeight = map.getHeight();
        }

        if (placementResolved && map.canStandAt(x, y, bounds.width, bounds.height)) {
            return;
        }

        if (!isInsideMapBounds(map, x, y)) {
            placementResolved = true;
            return;
        }

        Point standable = findNearestStandablePosition(map, x, y, 24);
        if (standable != null) {
            x = standable.x;
            y = standable.y;
            spawnX = x;
            spawnY = y;
            patrolTargetX = spawnX + getPatrolRange();
            velocityY = 0;
            onGround = true;
            updateBounds();
        }

        placementResolved = true;
    }

    boolean startDodgeIfThreatened(List<Bullet> bullets) {
        if (dodgeCooldownTimer > 0 || bullets == null || bullets.isEmpty()) {
            return false;
        }

        Rectangle dangerZone = new Rectangle(bounds);
        dangerZone.grow(DODGE_GROW_X, DODGE_GROW_Y);

        for (Bullet bullet : bullets) {
            Rectangle bulletBounds = bullet.getBounds();
            if (!dangerZone.intersects(bulletBounds)) {
                continue;
            }

            dodgeDirection = Math.signum(getCenterX() - bulletBounds.getCenterX());
            if (dodgeDirection == 0) {
                dodgeDirection = -facingDirection;
            }

            dodgeTimer = DODGE_DURATION_FRAMES;
            dodgeCooldownTimer = DODGE_COOLDOWN_FRAMES;
            return true;
        }

        return false;
    }

    boolean moveHorizontal(double dx, CollisionMap map, boolean needsSupport) {
        int steps = Math.max(1, (int)Math.ceil(Math.abs(dx)));
        double stepX = dx / steps;
        boolean moved = false;

        for (int i = 0; i < steps; i++) {
            double nextX = x + stepX;
            if (canMoveTo(nextX, y, map, needsSupport)) {
                x = nextX;
                moved = true;
            } else {
                return moved;
            }
        }

        updateBounds();
        return moved;
    }

    void applyVerticalPhysics(CollisionMap map) {
        if (onGround && velocityY >= 0) {
            return;
        }

        velocityY = Math.min(MAX_FALL_SPEED, velocityY + GRAVITY);
        int steps = Math.max(1, (int)Math.ceil(Math.abs(velocityY)));
        double stepY = velocityY / steps;

        for (int i = 0; i < steps; i++) {
            double nextY = y + stepY;
            if (map.isAreaSolid(x, nextY, bounds.width, bounds.height)) {
                if (velocityY > 0) {
                    onGround = true;
                }
                velocityY = 0;
                return;
            }
            y = nextY;
        }

        onGround = map.canStandAt(x, y, bounds.width, bounds.height);
        if (onGround && velocityY > 0) {
            velocityY = 0;
        }
    }

    void setFacingDirection(double direction) {
        if (direction != 0) {
            facingDirection = direction;
        }
    }

    double getFacingDirection() {
        return facingDirection;
    }

    int getMonsterWidth() {
        return bounds.width;
    }

    int getMonsterHeight() {
        return bounds.height;
    }

    double getX() {
        return x;
    }

    double getY() {
        return y;
    }

    void setY(double y) {
        this.y = y;
    }

    double getVelocityY() {
        return velocityY;
    }

    void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    boolean isOnGround() {
        return onGround;
    }

    void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    double getSpawnX() {
        return spawnX;
    }

    double getSpawnY() {
        return spawnY;
    }

    double getPatrolTargetX() {
        return patrolTargetX;
    }

    void setPatrolTargetX(double patrolTargetX) {
        this.patrolTargetX = patrolTargetX;
    }

    int getPatrolDirection() {
        return patrolDirection;
    }

    void setPatrolDirection(int patrolDirection) {
        this.patrolDirection = patrolDirection;
    }

    void clearDodgeState() {
        dodgeTimer = 0;
        dodgeDirection = 0;
    }

    boolean isDodgeActive() {
        return dodgeTimer > 0;
    }

    int getDodgeTimer() {
        return dodgeTimer;
    }

    void decrementDodgeTimer() {
        if (dodgeTimer > 0) {
            dodgeTimer--;
        }
    }

    void resetDodgeCooldown() {
        dodgeCooldownTimer = DODGE_COOLDOWN_FRAMES;
    }

    private boolean isInsideMapBounds(CollisionMap map, double candidateX, double candidateY) {
        return candidateX >= 0
            && candidateY >= 0
            && candidateX + bounds.width < map.getWidth()
            && candidateY + bounds.height < map.getHeight();
    }

    private Point findNearestStandablePosition(CollisionMap map, double originX, double originY, int maxRadiusTiles) {
        if (map.canStandAt(originX, originY, bounds.width, bounds.height)) {
            return new Point((int)Math.round(originX), (int)Math.round(originY));
        }

        double baseX = clamp(originX, 0, Math.max(0, map.getWidth() - bounds.width - 1));
        double baseY = clamp(originY, 0, Math.max(0, map.getHeight() - bounds.height - 1));
        Point best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int radius = 0; radius <= maxRadiusTiles; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    if (Math.abs(dx) != radius && Math.abs(dy) != radius) {
                        continue;
                    }

                    int candidateX = (int)Math.round(baseX + dx * TILE_SIZE);
                    int candidateY = (int)Math.round(baseY + dy * TILE_SIZE);

                    if (!map.canStandAt(candidateX, candidateY, bounds.width, bounds.height)) {
                        continue;
                    }

                    double distance = Math.hypot(candidateX - originX, candidateY - originY);
                    if (distance < bestDistance) {
                        best = new Point(candidateX, candidateY);
                        bestDistance = distance;
                    }
                }
            }

            if (best != null) {
                return best;
            }
        }

        return null;
    }

    private boolean canMoveTo(double nextX, double nextY, CollisionMap map, boolean needsSupport) {
        if (map.isAreaSolid(nextX, nextY, bounds.width, bounds.height)) {
            return false;
        }
        return !needsSupport || map.canStandAt(nextX, nextY, bounds.width, bounds.height);
    }

    private void updateBounds() {
        bounds.x = (int)Math.round(x);
        bounds.y = (int)Math.round(y);
    }

    private double getCenterX() {
        return x + bounds.width / 2.0;
    }

    private double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private double getPatrolRange() {
        return 120.0;
    }
}