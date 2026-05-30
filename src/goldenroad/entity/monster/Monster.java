package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionMap;
import goldenroad.map.GridPathfinder;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Monster {
    public enum State {
        IDLE,
        PATROL,
        CHASE,
        ATTACK,
        RETREAT
    }

    public enum Type {
        GROUND
    }

    private static final int ATTACK_ACTIVE_FRAMES = 12;
    private static final int ATTACK_DAMAGE = 3;
    private static final int ATTACK_COOLDOWN_FRAMES = 60;
    private static final int RETREAT_DURATION_FRAMES = 120;
    private static final int PATH_REFRESH_FRAMES = 18;
    private static final int PATROL_IDLE_FRAMES = 35;
    private static final int MAX_SEARCH_NODES = 300;
    private static final double ARRIVE_DISTANCE = 4.0;
    private static final double DETECTION_RANGE = 260.0;
    private static final double ATTACK_RANGE = 48.0;
    private static final double PATROL_RANGE = 120.0;
    private static final double MOVE_SPEED = 2.0;
    private static final double LOW_HEALTH_RETREAT_RATIO = 0.4;

    private final Rectangle bounds;
    private final Color color;
    private final Type type;
    private final MonsterMotion motion;
    private final GridPathfinder pathfinder = new GridPathfinder(16);
    private final int maxHealth;
    private final int retreatHealthThreshold;
    private int health;

    private State state = State.IDLE;
    private int idleTimer = PATROL_IDLE_FRAMES;
    private int attackCooldownTimer = 0;
    private int attackActiveTimer = 0;
    private int retreatTimer = 0;
    private int pathRefreshTimer = 0;

    private final List<Point> path = new ArrayList<>();
    private int pathIndex = 0;
    public Monster(int x, int y, int width, int height, Color color) {
        this(x, y, width, height, color, 3, Type.GROUND);
    }

    public Monster(int x, int y, int width, int height, Color color, int health) {
        this(x, y, width, height, color, health, Type.GROUND);
    }

    public Monster(int x, int y, int width, int height, Color color, int health, Type type) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = color;
        this.type = type;
        this.health = Math.max(1, health);
        this.maxHealth = this.health;
        this.retreatHealthThreshold = Math.max(1, (int)Math.ceil(this.maxHealth * LOW_HEALTH_RETREAT_RATIO));
        this.motion = new MonsterMotion(x, y, width, height, PATROL_RANGE);
    }
    private MonsterBehavior behavior = null;

    public int update(Player player, CollisionMap map) {
        return update(player, map, Collections.emptyList());
    }

    public int update(Player player, CollisionMap map, List<Bullet> bullets) {
        if (behavior != null) {
            return behavior.update(this, player, map, bullets);
        }
        return performDefaultUpdate(player, map, bullets);
    }

    /**
     * Existing default update logic extracted so behaviors can delegate or replace it.
     */
    int performDefaultUpdate(Player player, CollisionMap map, List<Bullet> bullets) {
        if (player == null || map == null || health <= 0) {
            return 0;
        }

        motion.resolvePlacement(map);
        updateTimers();
        motion.refreshGroundState(map);
        motion.startDodgeIfThreatened(bullets);

        if (motion.isDodgeActive()) {
            state = State.RETREAT;
            motion.updateDodgeStep(map);
            return 0;
        }

        chooseState(player);

        int damage = 0;
        switch (state) {
            case IDLE -> updateIdle();
            case PATROL -> updatePatrol(map);
            case CHASE -> updateChase(player, map);
            case ATTACK -> damage = updateAttack(player);
            case RETREAT -> updateRetreat(player, map);
        }

        applyVerticalPhysics(map);
        updateBounds();
        return damage;
    }

    public void setBehavior(MonsterBehavior behavior) {
        this.behavior = behavior;
    }

    public MonsterBehavior getBehavior() {
        return this.behavior;
    }

    public void draw(Graphics2D g) {
        Rectangle r = getBounds();
        g.setColor(getRenderColor());
        g.fillRect(r.x, r.y, r.width, r.height);

        if (isAttackActive()) {
            g.setColor(Color.WHITE);
            g.drawRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
        }

        if (health < maxHealth) {
            int barWidth = r.width;
            int barHeight = 4;
            int hpWidth = Math.max(1, (int)Math.round(barWidth * getHealthRatio()));

            g.setColor(new Color(35, 20, 25));
            g.fillRect(r.x, r.y - 8, barWidth, barHeight);
            g.setColor(new Color(80, 220, 120));
            g.fillRect(r.x, r.y - 8, hpWidth, barHeight);
        }
    }

    public void render(Graphics2D g) {
        draw(g);
    }

    private void updateTimers() {
        if (attackCooldownTimer > 0) {
            attackCooldownTimer--;
        }
        if (attackActiveTimer > 0) {
            attackActiveTimer--;
        }
        if (pathRefreshTimer > 0) {
            pathRefreshTimer--;
        }
        motion.updateTimers();
    }

    private void chooseState(Player player) {
        double distance = distanceToPlayer(player);

        if (isLowHealth() && distance <= DETECTION_RANGE * 1.35) {
            enterState(State.RETREAT);
            retreatTimer = Math.max(retreatTimer, RETREAT_DURATION_FRAMES);
            return;
        }

        if (distance <= ATTACK_RANGE) {
            enterState(State.ATTACK);
            return;
        }

        if (distance <= DETECTION_RANGE) {
            enterState(State.CHASE);
            return;
        }

        if (state == State.CHASE || state == State.ATTACK || state == State.RETREAT) {
            enterState(State.IDLE);
        }
    }

    private void updateIdle() {
        if (idleTimer > 0) {
            idleTimer--;
            return;
        }

        state = State.PATROL;
        motion.setPatrolTargetX(motion.getSpawnX() + motion.getPatrolDirection() * PATROL_RANGE);
    }

    private void updatePatrol(CollisionMap map) {
        boolean reachedTarget = Math.abs(getCenterX() - motion.getPatrolTargetX()) <= ARRIVE_DISTANCE;
        if (reachedTarget) {
            pauseAndReversePatrol();
            return;
        }

        double direction = Math.signum(motion.getPatrolTargetX() - getCenterX());
        if (direction == 0) {
            direction = motion.getPatrolDirection();
        }

        motion.setFacingDirection(direction);
        boolean moved = motion.moveHorizontal(direction * MOVE_SPEED, map, true);
        if (!moved) {
            pauseAndReversePatrol();
        }
    }

    private void updateChase(Player player, CollisionMap map) {
        if (shouldRefreshPath()) {
            path.clear();
            path.addAll(pathfinder.findPath(
                map,
                bounds.width,
                bounds.height,
                getCenterX(),
                getCenterY(),
                playerCenterX(player),
                playerCenterY(player),
                MAX_SEARCH_NODES
            ));
            pathIndex = 0;
            pathRefreshTimer = PATH_REFRESH_FRAMES;
        }

        if (!followPath(map)) {
            moveTowardPoint(
                playerCenterX(player) - bounds.width / 2.0,
                player.getY() + player.getHeight() - bounds.height,
                MOVE_SPEED,
                map,
                false
            );
        }
    }

    private int updateAttack(Player player) {
        clearPath();
        faceTarget(playerCenterX(player));

        if (distanceToPlayer(player) > ATTACK_RANGE * 1.15) {
            enterState(State.CHASE);
            return 0;
        }

        if (attackCooldownTimer <= 0) {
            attackActiveTimer = ATTACK_ACTIVE_FRAMES;
            attackCooldownTimer = ATTACK_COOLDOWN_FRAMES;
            return ATTACK_DAMAGE;
        }

        return 0;
    }

    private void updateRetreat(Player player, CollisionMap map) {
        clearPath();
        retreatTimer--;

        double away = Math.signum(getCenterX() - playerCenterX(player));
        if (away == 0) {
            away = -motion.getFacingDirection();
        }

        motion.setFacingDirection(away);
        boolean moved = motion.moveHorizontal(away * 2.6, map, true);
        if (!moved) {
            moveTowardPoint(motion.getSpawnX(), motion.getSpawnY(), MOVE_SPEED, map, false);
        }

        if (retreatTimer <= 0 && distanceToPlayer(player) > DETECTION_RANGE) {
            enterState(State.IDLE);
        }
    }

    private boolean shouldRefreshPath() {
        return pathRefreshTimer <= 0 || path.isEmpty();
    }

    private boolean followPath(CollisionMap map) {
        if (path.isEmpty() || pathIndex >= path.size()) {
            return false;
        }

        Point next = path.get(pathIndex);
        double targetX = next.x - bounds.width / 2.0;
        double targetY = next.y - bounds.height;

        if (Math.hypot(targetX - motion.getX(), targetY - motion.getY()) <= ARRIVE_DISTANCE) {
            pathIndex++;
            if (pathIndex >= path.size()) {
                return true;
            }
            next = path.get(pathIndex);
            targetX = next.x - bounds.width / 2.0;
            targetY = next.y - bounds.height;
        }

        return moveTowardPoint(targetX, targetY, MOVE_SPEED, map, false);
    }

    private boolean moveTowardPoint(double targetX, double targetY, double speed, CollisionMap map, boolean needsSupport) {
        double dx = targetX - motion.getX();
        if (Math.abs(dx) <= ARRIVE_DISTANCE && Math.abs(targetY - motion.getY()) <= ARRIVE_DISTANCE) {
            return true;
        }

        double direction = Math.signum(dx);
        if (direction == 0) {
            return false;
        }

        faceTarget(targetX + bounds.width / 2.0);
        return motion.moveHorizontal(direction * speed, map, needsSupport);
    }

    boolean moveHorizontal(double dx, CollisionMap map, boolean needsSupport) {
        return motion.moveHorizontal(dx, map, needsSupport);
    }

    void applyVerticalPhysics(CollisionMap map) {
        motion.applyVerticalPhysics(map);
    }

    void setFacingDirection(double direction) {
        motion.setFacingDirection(direction);
    }

    double getFacingDirection() {
        return motion.getFacingDirection();
    }

    int getMonsterWidth() {
        return motion.getMonsterWidth();
    }

    int getMonsterHeight() {
        return motion.getMonsterHeight();
    }

    private void pauseAndReversePatrol() {
        motion.setPatrolDirection(motion.getPatrolDirection() * -1);
        idleTimer = PATROL_IDLE_FRAMES;
        state = State.IDLE;
        clearPath();
    }

    private void clearPath() {
        path.clear();
        pathIndex = 0;
        pathRefreshTimer = 0;
    }

    private void enterState(State nextState) {
        if (state == nextState) {
            return;
        }

        state = nextState;
        clearPath();
        if (state == State.IDLE) {
            idleTimer = PATROL_IDLE_FRAMES;
        }
        if (state == State.RETREAT) {
            retreatTimer = Math.max(retreatTimer, RETREAT_DURATION_FRAMES);
        }
    }

    private void faceTarget(double targetCenterX) {
        double direction = Math.signum(targetCenterX - getCenterX());
        if (direction != 0) {
            motion.setFacingDirection(direction);
        }
    }

    private void updateBounds() {
        bounds.x = (int)Math.round(motion.getX());
        bounds.y = (int)Math.round(motion.getY());
    }

    private double distanceToPlayer(Player player) {
        return Math.hypot(playerCenterX(player) - getCenterX(), playerCenterY(player) - getCenterY());
    }

    private double clamp(double value, double min, double max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private double playerCenterX(Player player) {
        return player.getX() + player.getWidth() / 2.0;
    }

    private double playerCenterY(Player player) {
        return player.getY() + player.getHeight() / 2.0;
    }

    private double getCenterX() {
        return motion.getX() + bounds.width / 2.0;
    }

    private double getCenterY() {
        return motion.getY() + bounds.height / 2.0;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Color getColor() {
        return color;
    }

    public Color getRenderColor() {
        return switch (state) {
            case IDLE -> color.darker();
            case PATROL -> color;
            case CHASE -> new Color(235, 145, 70);
            case ATTACK -> new Color(245, 70, 70);
            case RETREAT -> isLowHealth() ? new Color(80, 150, 240) : new Color(110, 180, 235);
        };
    }

    public Type getType() {
        return type;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public double getHealthRatio() {
        return Math.max(0, Math.min(1, health / (double)maxHealth));
    }

    public boolean isLowHealth() {
        return health <= retreatHealthThreshold && health < maxHealth;
    }

    public State getState() {
        return state;
    }

    public double getX() {
        return motion.getX();
    }

    public double getY() {
        return motion.getY();
    }

    public double getDetectionRange() {
        return DETECTION_RANGE;
    }

    public double getAttackRange() {
        return ATTACK_RANGE;
    }

    public boolean isAttackActive() {
        return attackActiveTimer > 0;
    }

    void clearPathAndDodge() {
        clearPath();
        motion.clearDodgeState();
    }

    void syncBoundsFromMotion() {
        updateBounds();
    }

    public boolean takeDamage(int damage) {
        if (damage <= 0 || health <= 0) {
            return false;
        }

        health = Math.max(0, health - damage);
        if (isLowHealth()) {
            enterState(State.RETREAT);
        }
        return health <= 0;
    }
}
