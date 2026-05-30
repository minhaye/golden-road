package goldenroad.entity;

import goldenroad.map.CollisionMap;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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

    private static final int TILE_SIZE = 16;
    private static final int ATTACK_ACTIVE_FRAMES = 12;
    private static final int ATTACK_DAMAGE = 3;
    private static final int ATTACK_COOLDOWN_FRAMES = 60;
    private static final int RETREAT_DURATION_FRAMES = 120;
    private static final int PATH_REFRESH_FRAMES = 18;
    private static final int PATROL_IDLE_FRAMES = 35;
    private static final int MAX_SEARCH_NODES = 300;
    private static final int PATH_CACHE_LIMIT = 72;
    private static final double ARRIVE_DISTANCE = 4.0;
    private static final double GRAVITY = 0.7;
    private static final double MAX_FALL_SPEED = 9.0;
    private static final double DETECTION_RANGE = 260.0;
    private static final double ATTACK_RANGE = 48.0;
    private static final double PATROL_RANGE = 120.0;
    private static final double MOVE_SPEED = 2.0;
    private static final double RETREAT_SPEED = 2.6;
    private static final double LOW_HEALTH_RETREAT_RATIO = 0.4;

    private static final Map<String, List<Point>> PATH_CACHE = new LinkedHashMap<>(PATH_CACHE_LIMIT, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Point>> eldest) {
            return size() > PATH_CACHE_LIMIT;
        }
    };

    private final Rectangle bounds;
    private final Color color;
    private final Type type;
    private final int maxHealth;
    private final int retreatHealthThreshold;
    private int health;

    private double x;
    private double y;
    private double velocityY = 0;
    private boolean onGround = true;
    private double spawnX;
    private double spawnY;
    private double patrolTargetX;
    private int patrolDirection = 1;
    private double facingDirection = 1;

    private State state = State.IDLE;
    private int idleTimer = PATROL_IDLE_FRAMES;
    private int attackCooldownTimer = 0;
    private int attackActiveTimer = 0;
    private int retreatTimer = 0;
    private int pathRefreshTimer = 0;
    private int dodgeCooldownTimer = 0;
    private int dodgeTimer = 0;
    private double dodgeDirection = 0;
    private boolean placementResolved = false;
    private int resolvedMapWidth = -1;
    private int resolvedMapHeight = -1;

    private final List<Point> path = new ArrayList<>();
    private int pathIndex = 0;
    private Point lastTargetTile = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

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
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.patrolTargetX = x + PATROL_RANGE;
    }

    public int update(Player player, CollisionMap map) {
        return update(player, map, Collections.emptyList());
    }

    public int update(Player player, CollisionMap map, List<Bullet> bullets) {
        if (player == null || map == null || health <= 0) {
            return 0;
        }

        resolvePlacement(map);
        updateTimers();
        refreshGroundState(map);
        startDodgeIfThreatened(bullets);

        if (dodgeTimer > 0) {
            state = State.RETREAT;
            dodgeTimer--;
            moveHorizontal(dodgeDirection * RETREAT_SPEED * 1.15, map, true);
            applyVerticalPhysics(map);
            updateBounds();
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
        if (dodgeCooldownTimer > 0) {
            dodgeCooldownTimer--;
        }
    }

    private void refreshGroundState(CollisionMap map) {
        boolean standing = map.canStandAt(x, y, bounds.width, bounds.height);
        if (standing && velocityY >= 0) {
            onGround = true;
            velocityY = 0;
        } else if (!standing) {
            onGround = false;
        }
    }

    private void resolvePlacement(CollisionMap map) {
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
            patrolTargetX = spawnX + patrolDirection * PATROL_RANGE;
            velocityY = 0;
            onGround = true;
            clearPath();
            updateBounds();
        }

        placementResolved = true;
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

    private void startDodgeIfThreatened(List<Bullet> bullets) {
        if (dodgeCooldownTimer > 0 || bullets == null || bullets.isEmpty()) {
            return;
        }

        Rectangle dangerZone = new Rectangle(bounds);
        dangerZone.grow(72, 18);

        for (Bullet bullet : bullets) {
            Rectangle bulletBounds = bullet.getBounds();
            if (!dangerZone.intersects(bulletBounds)) {
                continue;
            }

            dodgeDirection = Math.signum(getCenterX() - bulletBounds.getCenterX());
            if (dodgeDirection == 0) {
                dodgeDirection = -facingDirection;
            }

            dodgeTimer = 12;
            dodgeCooldownTimer = 80;
            clearPath();
            return;
        }
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
        patrolTargetX = spawnX + patrolDirection * PATROL_RANGE;
    }

    private void updatePatrol(CollisionMap map) {
        boolean reachedTarget = Math.abs(getCenterX() - patrolTargetX) <= ARRIVE_DISTANCE;
        if (reachedTarget) {
            pauseAndReversePatrol();
            return;
        }

        double direction = Math.signum(patrolTargetX - getCenterX());
        if (direction == 0) {
            direction = patrolDirection;
        }

        facingDirection = direction;
        boolean moved = moveHorizontal(direction * MOVE_SPEED, map, true);
        if (!moved) {
            pauseAndReversePatrol();
        }
    }

    private void updateChase(Player player, CollisionMap map) {
        Point targetTile = getNearestWalkableTile(map, getPlayerTile(player), 6);
        Point currentTile = getNearestWalkableTile(map, getCurrentTile(), 3);

        if (targetTile != null && currentTile != null && shouldRefreshPath(targetTile)) {
            path.clear();
            path.addAll(findPath(map, currentTile, targetTile));
            pathIndex = 0;
            lastTargetTile = targetTile;
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
            away = -facingDirection;
        }

        facingDirection = away;
        boolean moved = moveHorizontal(away * RETREAT_SPEED, map, true);
        if (!moved) {
            moveTowardPoint(spawnX, spawnY, MOVE_SPEED, map, false);
        }

        if (retreatTimer <= 0 && distanceToPlayer(player) > DETECTION_RANGE) {
            enterState(State.IDLE);
        }
    }

    private boolean shouldRefreshPath(Point targetTile) {
        return pathRefreshTimer <= 0 || path.isEmpty() || !targetTile.equals(lastTargetTile);
    }

    private boolean followPath(CollisionMap map) {
        if (path.isEmpty() || pathIndex >= path.size()) {
            return false;
        }

        Point next = path.get(pathIndex);
        double targetX = tileCenterX(next.x) - bounds.width / 2.0;
        double targetY = tileFeetY(next.y) - bounds.height;

        if (Math.hypot(targetX - x, targetY - y) <= ARRIVE_DISTANCE) {
            pathIndex++;
            if (pathIndex >= path.size()) {
                return true;
            }
            next = path.get(pathIndex);
            targetX = tileCenterX(next.x) - bounds.width / 2.0;
            targetY = tileFeetY(next.y) - bounds.height;
        }

        return moveTowardPoint(targetX, targetY, MOVE_SPEED, map, false);
    }

    private List<Point> findPath(CollisionMap map, Point start, Point goal) {
        if (start.equals(goal)) {
            return Collections.emptyList();
        }

        String cacheKey = start.x + "," + start.y + ">" + goal.x + "," + goal.y + ":" + bounds.width + "x" + bounds.height;
        List<Point> cachedPath = PATH_CACHE.get(cacheKey);
        if (cachedPath != null) {
            return new ArrayList<>(cachedPath);
        }

        int widthTiles = Math.max(1, (int)Math.ceil(map.getWidth() / (double)TILE_SIZE));
        int heightTiles = Math.max(1, (int)Math.ceil(map.getHeight() / (double)TILE_SIZE));
        if (!isInsideTileMap(start, widthTiles, heightTiles) || !isInsideTileMap(goal, widthTiles, heightTiles)) {
            return Collections.emptyList();
        }

        boolean[][] visited = new boolean[widthTiles][heightTiles];
        Point[][] parent = new Point[widthTiles][heightTiles];
        Queue<Point> open = new ArrayDeque<>();

        open.add(start);
        visited[start.x][start.y] = true;
        int searchedNodes = 0;
        Point best = start;
        int[][] directions = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1}
        };

        while (!open.isEmpty() && searchedNodes < MAX_SEARCH_NODES) {
            Point current = open.remove();
            searchedNodes++;

            if (current.distance(goal) < best.distance(goal)) {
                best = current;
            }
            if (current.equals(goal)) {
                return cachePath(cacheKey, buildPath(parent, start, goal));
            }

            for (int[] direction : directions) {
                int nextX = current.x + direction[0];
                int nextY = current.y + direction[1];

                if (nextX < 0 || nextY < 0 || nextX >= widthTiles || nextY >= heightTiles) {
                    continue;
                }
                if (visited[nextX][nextY] || !isWalkableTile(map, nextX, nextY)) {
                    continue;
                }

                Point next = new Point(nextX, nextY);
                visited[nextX][nextY] = true;
                parent[nextX][nextY] = current;
                open.add(next);
            }
        }

        if (!best.equals(start)) {
            return cachePath(cacheKey, buildPath(parent, start, best));
        }
        return Collections.emptyList();
    }

    private List<Point> cachePath(String key, List<Point> result) {
        PATH_CACHE.put(key, new ArrayList<>(result));
        return result;
    }

    private List<Point> buildPath(Point[][] parent, Point start, Point goal) {
        List<Point> result = new ArrayList<>();
        Point current = goal;

        while (current != null && !current.equals(start)) {
            result.add(current);
            current = parent[current.x][current.y];
        }

        Collections.reverse(result);
        return result;
    }

    private Point getNearestWalkableTile(CollisionMap map, Point origin, int radius) {
        if (origin == null) {
            return null;
        }

        if (isWalkableTile(map, origin.x, origin.y)) {
            return origin;
        }

        for (int distance = 1; distance <= radius; distance++) {
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    if (Math.abs(dx) != distance && Math.abs(dy) != distance) {
                        continue;
                    }

                    Point candidate = new Point(origin.x + dx, origin.y + dy);
                    if (isWalkableTile(map, candidate.x, candidate.y)) {
                        return candidate;
                    }
                }
            }
        }

        return null;
    }

    private boolean isWalkableTile(CollisionMap map, int tileX, int tileY) {
        double worldX = tileCenterX(tileX) - bounds.width / 2.0;
        double worldY = tileFeetY(tileY) - bounds.height;
        return map.canStandAt(worldX, worldY, bounds.width, bounds.height);
    }

    private boolean isInsideTileMap(Point point, int widthTiles, int heightTiles) {
        return point.x >= 0 && point.y >= 0 && point.x < widthTiles && point.y < heightTiles;
    }

    private boolean moveTowardPoint(double targetX, double targetY, double speed, CollisionMap map, boolean needsSupport) {
        double dx = targetX - x;
        if (Math.abs(dx) <= ARRIVE_DISTANCE && Math.abs(targetY - y) <= ARRIVE_DISTANCE) {
            return true;
        }

        double direction = Math.signum(dx);
        if (direction == 0) {
            return false;
        }

        faceTarget(targetX + bounds.width / 2.0);
        return moveHorizontal(direction * speed, map, needsSupport);
    }

    private boolean moveHorizontal(double dx, CollisionMap map, boolean needsSupport) {
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

    private void applyVerticalPhysics(CollisionMap map) {
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

    private boolean canMoveTo(double nextX, double nextY, CollisionMap map, boolean needsSupport) {
        if (map.isAreaSolid(nextX, nextY, bounds.width, bounds.height)) {
            return false;
        }
        return !needsSupport || map.canStandAt(nextX, nextY, bounds.width, bounds.height);
    }

    private void pauseAndReversePatrol() {
        patrolDirection *= -1;
        idleTimer = PATROL_IDLE_FRAMES;
        state = State.IDLE;
        clearPath();
    }

    private void clearPath() {
        path.clear();
        pathIndex = 0;
        lastTargetTile = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);
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
            facingDirection = direction;
        }
    }

    private void updateBounds() {
        bounds.x = (int)Math.round(x);
        bounds.y = (int)Math.round(y);
    }

    private Point getCurrentTile() {
        return new Point(
            (int)Math.floor(getCenterX() / TILE_SIZE),
            (int)Math.round((y + bounds.height) / TILE_SIZE)
        );
    }

    private Point getPlayerTile(Player player) {
        return new Point(
            (int)Math.floor(playerCenterX(player) / TILE_SIZE),
            (int)Math.round((player.getY() + player.getHeight()) / TILE_SIZE)
        );
    }

    private double tileCenterX(int tileX) {
        return tileX * TILE_SIZE + TILE_SIZE / 2.0;
    }

    private double tileFeetY(int tileY) {
        return tileY * TILE_SIZE;
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
        return x + bounds.width / 2.0;
    }

    private double getCenterY() {
        return y + bounds.height / 2.0;
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
        return x;
    }

    public double getY() {
        return y;
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
