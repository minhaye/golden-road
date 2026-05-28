package goldenroad.entity;

import goldenroad.map.CollisionMap;
import goldenroad.map.GridPathfinder;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Monster {
    public enum EnemyState {
        IDLE, PATROL, CHASE, ATTACK, RETREAT
    }

    private static final double PATROL_SPEED = 1.1;
    private static final double CHASE_SPEED = 2.0;
    private static final double RETREAT_SPEED = 2.4;
    private static final double ARRIVAL_DISTANCE = 6.0;

    private static final int DEFAULT_DETECTION_RANGE = 320;
    private static final int DEFAULT_ATTACK_RANGE = 48;
    private static final int ATTACK_DAMAGE = 8;
    private static final int ATTACK_COOLDOWN_FRAMES = 65;
    private static final int IDLE_FRAMES = 45;
    private static final int RETREAT_AFTER_ATTACK_FRAMES = 28;
    private static final int RETREAT_WHEN_CLOSE_RANGE = 28;
    private static final int RETREAT_TARGET_DISTANCE = 150;
    private static final int PATH_REFRESH_FRAMES = 18;
    private static final int PATH_TARGET_REFRESH_DISTANCE = 24;
    private static final int MAX_PATH_NODES = 900;
    private static final double LOW_HEALTH_RETREAT_RATIO = 0.35;

    private final Rectangle bounds;
    private final Color color;
    private final int maxHealth;
    private final List<Point> patrolPoints = new ArrayList<>();

    private double x;
    private double y;
    private final double spawnCenterX;
    private final double spawnCenterY;

    private int health;
    private int detectionRange = DEFAULT_DETECTION_RANGE;
    private int attackRange = DEFAULT_ATTACK_RANGE;
    private int attackCooldown = 0;
    private int stateTimer = 0;
    private int currentPatrolIndex = 0;

    private EnemyState state = EnemyState.PATROL;

    private List<Point> path = Collections.emptyList();
    private int pathIndex = 0;
    private int pathRefreshTimer = 0;
    private double lastPathTargetX = Double.NaN;
    private double lastPathTargetY = Double.NaN;

    public Monster(int x, int y, int width, int height, Color color) {
        this(x, y, width, height, color, 3);
    }

    public Monster(int x, int y, int width, int height, Color color, int health) {
        this.x = x;
        this.y = y;
        this.bounds = new Rectangle(x, y, width, height);
        this.color = color;
        this.maxHealth = Math.max(1, health);
        this.health = this.maxHealth;
        this.spawnCenterX = x + width / 2.0;
        this.spawnCenterY = y + height / 2.0;
        setupDefaultPatrolRoute();
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Color getColor() {
        return color;
    }

    public int getHealth() {
        return health;
    }

    public EnemyState getState() {
        return state;
    }

    public void setRanges(int detectionRange, int attackRange) {
        int normalizedAttackRange = Math.max(1, attackRange);
        this.attackRange = normalizedAttackRange;
        this.detectionRange = Math.max(normalizedAttackRange + 1, detectionRange);
    }

    public void setPatrolRoute(List<Point> route) {
        patrolPoints.clear();
        if (route != null) {
            patrolPoints.addAll(route);
        }
        currentPatrolIndex = 0;
    }

    public boolean takeDamage(int damage) {
        if (damage <= 0 || health <= 0) {
            return false;
        }

        health -= damage;

        if (health > 0) {
            if (isLowHealth()) {
                setState(EnemyState.RETREAT);
                stateTimer = RETREAT_AFTER_ATTACK_FRAMES;
            } else {
                setState(EnemyState.CHASE);
            }
        }

        return health <= 0;
    }

    public int update(Player player, CollisionMap collisionMap, GridPathfinder pathfinder) {
        if (health <= 0) {
            return 0;
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (stateTimer > 0) {
            stateTimer--;
        }

        Rectangle playerBounds = new Rectangle(
            (int) player.getX(),
            (int) player.getY(),
            (int) player.getWidth(),
            (int) player.getHeight()
        );

        double playerCenterX = playerBounds.getCenterX();
        double playerCenterY = playerBounds.getCenterY();
        double distanceToPlayer = distance(centerX(), centerY(), playerCenterX, playerCenterY);

        updateState(distanceToPlayer);

        int damage = 0;
        switch (state) {
            case IDLE -> updateIdle();
            case PATROL -> updatePatrol(collisionMap);
            case CHASE -> updateChase(playerCenterX, playerCenterY, collisionMap, pathfinder);
            case ATTACK -> damage = updateAttack(distanceToPlayer);
            case RETREAT -> updateRetreat(playerCenterX, playerCenterY, distanceToPlayer, collisionMap, pathfinder);
        }

        syncBounds();
        return damage;
    }

    public void draw(Graphics2D g) {
        Color bodyColor = getStateColor();
        int bodyX = bounds.x;
        int bodyY = bounds.y;
        int bodyW = bounds.width;
        int bodyH = bounds.height;
        int wingY = bodyY + bodyH / 3;

        g.setColor(new Color(140, 95, 190, 170));
        Polygon leftWing = new Polygon(
            new int[] {bodyX + bodyW / 4, bodyX - bodyW / 2, bodyX + bodyW / 6},
            new int[] {wingY, wingY + bodyH / 5, wingY + bodyH / 2},
            3
        );
        Polygon rightWing = new Polygon(
            new int[] {bodyX + bodyW * 3 / 4, bodyX + bodyW + bodyW / 2, bodyX + bodyW * 5 / 6},
            new int[] {wingY, wingY + bodyH / 5, wingY + bodyH / 2},
            3
        );
        g.fillPolygon(leftWing);
        g.fillPolygon(rightWing);

        g.setColor(bodyColor);
        g.fillOval(bodyX, bodyY, bodyW, bodyH);

        g.setColor(new Color(20, 24, 34));
        int eyeSize = Math.max(3, bodyW / 8);
        g.fillOval(bodyX + bodyW / 3, bodyY + bodyH / 3, eyeSize, eyeSize);
        g.fillOval(bodyX + bodyW * 2 / 3, bodyY + bodyH / 3, eyeSize, eyeSize);

        if (health < maxHealth) {
            int barWidth = bodyW;
            int barHeight = 4;
            int barY = bodyY - 8;
            int hpWidth = Math.max(1, (int) Math.round(barWidth * (health / (double) maxHealth)));

            g.setColor(new Color(35, 20, 25));
            g.fillRect(bodyX, barY, barWidth, barHeight);
            g.setColor(new Color(80, 220, 120));
            g.fillRect(bodyX, barY, hpWidth, barHeight);
        }
    }

    private void setupDefaultPatrolRoute() {
        int left = (int) Math.round(spawnCenterX - 96);
        int right = (int) Math.round(spawnCenterX + 96);
        int y = (int) Math.round(spawnCenterY);

        patrolPoints.add(new Point(left, y));
        patrolPoints.add(new Point(right, y));
    }

    private void updateState(double distanceToPlayer) {
        if (state == EnemyState.RETREAT && stateTimer > 0) {
            return;
        }

        if (shouldRetreat(distanceToPlayer)) {
            setState(EnemyState.RETREAT);
            return;
        }

        if (distanceToPlayer <= attackRange) {
            setState(EnemyState.ATTACK);
            return;
        }

        if (distanceToPlayer <= detectionRange) {
            setState(EnemyState.CHASE);
            return;
        }

        if (state == EnemyState.CHASE || state == EnemyState.ATTACK || state == EnemyState.RETREAT) {
            setState(EnemyState.PATROL);
        }

        if (state == EnemyState.IDLE && stateTimer <= 0) {
            setState(EnemyState.PATROL);
        }
    }

    private void updateIdle() {
        if (stateTimer <= 0) {
            setState(EnemyState.PATROL);
        }
    }

    private void updatePatrol(CollisionMap collisionMap) {
        if (patrolPoints.isEmpty()) {
            return;
        }

        Point target = patrolPoints.get(currentPatrolIndex);
        if (moveToward(target.x, target.y, PATROL_SPEED, collisionMap)) {
            currentPatrolIndex = (currentPatrolIndex + 1) % patrolPoints.size();
            setState(EnemyState.IDLE);
            stateTimer = IDLE_FRAMES;
        }
    }

    private void updateChase(double targetX, double targetY, CollisionMap collisionMap, GridPathfinder pathfinder) {
        followTarget(targetX, targetY, CHASE_SPEED, collisionMap, pathfinder);
    }

    private int updateAttack(double distanceToPlayer) {
        if (distanceToPlayer > attackRange * 1.25) {
            setState(EnemyState.CHASE);
            return 0;
        }

        if (attackCooldown > 0) {
            return 0;
        }

        attackCooldown = ATTACK_COOLDOWN_FRAMES;
        setState(EnemyState.RETREAT);
        stateTimer = RETREAT_AFTER_ATTACK_FRAMES;
        return ATTACK_DAMAGE;
    }

    private void updateRetreat(
        double playerCenterX,
        double playerCenterY,
        double distanceToPlayer,
        CollisionMap collisionMap,
        GridPathfinder pathfinder
    ) {
        double awayX = centerX() - playerCenterX;
        double awayY = centerY() - playerCenterY;
        double length = Math.hypot(awayX, awayY);

        if (length == 0) {
            awayX = centerX() - spawnCenterX;
            awayY = centerY() - spawnCenterY;
            length = Math.hypot(awayX, awayY);
        }

        if (length == 0) {
            awayX = 1;
            awayY = 0;
            length = 1;
        }

        double targetX = centerX() + (awayX / length) * RETREAT_TARGET_DISTANCE;
        double targetY = centerY() + (awayY / length) * RETREAT_TARGET_DISTANCE;

        if (collisionMap != null && collisionMap.isLoaded()) {
            targetX = clamp(targetX, bounds.width / 2.0, collisionMap.getWidth() - bounds.width / 2.0 - 1);
            targetY = clamp(targetY, bounds.height / 2.0, collisionMap.getHeight() - bounds.height / 2.0 - 1);
        }

        followTarget(targetX, targetY, RETREAT_SPEED, collisionMap, pathfinder);

        if (stateTimer <= 0 && !isLowHealth() && distanceToPlayer > attackRange * 1.5) {
            setState(distanceToPlayer <= detectionRange ? EnemyState.CHASE : EnemyState.PATROL);
        }
    }

    private void followTarget(
        double targetX,
        double targetY,
        double speed,
        CollisionMap collisionMap,
        GridPathfinder pathfinder
    ) {
        if (pathRefreshTimer > 0) {
            pathRefreshTimer--;
        }

        if (collisionMap == null || !collisionMap.isLoaded()
                || !collisionMap.isSegmentBlocked(centerX(), centerY(), targetX, targetY, bounds.width, bounds.height)) {
            clearPath();
            moveToward(targetX, targetY, speed, collisionMap);
            return;
        }

        boolean targetMoved = Double.isNaN(lastPathTargetX)
            || distance(lastPathTargetX, lastPathTargetY, targetX, targetY) > PATH_TARGET_REFRESH_DISTANCE;

        if (pathfinder != null && (pathRefreshTimer <= 0 || path.isEmpty() || targetMoved)) {
            path = pathfinder.findPath(
                collisionMap,
                bounds.width,
                bounds.height,
                centerX(),
                centerY(),
                targetX,
                targetY,
                MAX_PATH_NODES
            );
            pathIndex = 0;
            pathRefreshTimer = PATH_REFRESH_FRAMES;
            lastPathTargetX = targetX;
            lastPathTargetY = targetY;
        }

        if (path.isEmpty() || pathIndex >= path.size()) {
            moveToward(targetX, targetY, speed, collisionMap);
            return;
        }

        Point waypoint = path.get(pathIndex);
        if (distance(centerX(), centerY(), waypoint.x, waypoint.y) <= ARRIVAL_DISTANCE) {
            pathIndex++;
            if (pathIndex >= path.size()) {
                return;
            }
            waypoint = path.get(pathIndex);
        }

        moveToward(waypoint.x, waypoint.y, speed, collisionMap);
    }

    private boolean moveToward(double targetCenterX, double targetCenterY, double speed, CollisionMap collisionMap) {
        double dx = targetCenterX - centerX();
        double dy = targetCenterY - centerY();
        double length = Math.hypot(dx, dy);

        if (length <= ARRIVAL_DISTANCE) {
            return true;
        }

        double step = Math.min(speed, length);
        moveBy((dx / length) * step, (dy / length) * step, collisionMap);
        return false;
    }

    private void moveBy(double dx, double dy, CollisionMap collisionMap) {
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy))));
        double stepX = dx / steps;
        double stepY = dy / steps;

        for (int i = 0; i < steps; i++) {
            double nextX = x + stepX;
            double nextY = y + stepY;

            if (canMoveTo(nextX, nextY, collisionMap)) {
                x = nextX;
                y = nextY;
                continue;
            }

            boolean moved = false;
            if (canMoveTo(nextX, y, collisionMap)) {
                x = nextX;
                moved = true;
            }
            if (canMoveTo(x, nextY, collisionMap)) {
                y = nextY;
                moved = true;
            }

            if (!moved) {
                pathRefreshTimer = 0;
                break;
            }
        }
    }

    private boolean canMoveTo(double nextX, double nextY, CollisionMap collisionMap) {
        if (collisionMap == null || !collisionMap.isLoaded()) {
            return true;
        }

        return !collisionMap.isAreaSolid(
            (int) Math.floor(nextX),
            (int) Math.floor(nextY),
            bounds.width,
            bounds.height
        );
    }

    private boolean shouldRetreat(double distanceToPlayer) {
        return (isLowHealth() && distanceToPlayer <= detectionRange * 1.25)
            || distanceToPlayer <= RETREAT_WHEN_CLOSE_RANGE;
    }

    private boolean isLowHealth() {
        return health <= Math.max(1, Math.ceil(maxHealth * LOW_HEALTH_RETREAT_RATIO));
    }

    private void setState(EnemyState nextState) {
        if (state == nextState) {
            return;
        }

        state = nextState;
        clearPath();
    }

    private void clearPath() {
        path = Collections.emptyList();
        pathIndex = 0;
        pathRefreshTimer = 0;
        lastPathTargetX = Double.NaN;
        lastPathTargetY = Double.NaN;
    }

    private double centerX() {
        return x + bounds.width / 2.0;
    }

    private double centerY() {
        return y + bounds.height / 2.0;
    }

    private double distance(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    private double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private void syncBounds() {
        bounds.setLocation((int) Math.round(x), (int) Math.round(y));
    }

    private Color getStateColor() {
        return switch (state) {
            case IDLE -> color.darker();
            case PATROL -> color;
            case CHASE -> new Color(225, 85, 70);
            case ATTACK -> new Color(245, 205, 90);
            case RETREAT -> new Color(90, 150, 220);
        };
    }
}
