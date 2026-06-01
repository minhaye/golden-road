package goldenroad.entity.monster;

import goldenroad.entity.Entity;
import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionMap;
import goldenroad.map.GridPathfinder;
import goldenroad.util.AssetLoader;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class Monster extends Entity {
    private static final int MAX_ANIMATION_FRAMES = 64;
    private static final int PATH_TILE_SIZE = 16;
    private static final int PATH_MAX_VISITED_NODES = 2500;
    private static final GridPathfinder PATHFINDER = new GridPathfinder(PATH_TILE_SIZE);
    protected int hp;
    protected int damage;
    protected float attackSpeed;
    protected float moveSpeed;
    protected int width;
    protected int height;
    protected float spawnX;
    protected float spawnY;
    protected float moveRange;
    protected float detectRange;
    protected float attackRange;
    protected final Map<MonsterState, List<BufferedImage>> assets = new EnumMap<>(MonsterState.class);
    protected MonsterState currentState = MonsterState.IDLE;
    protected Direction direction = Direction.RIGHT;
    protected int currentFrame;
    protected float frameTimer;
    protected float frameDuration = 8f;
    protected boolean isDead;
    protected boolean isAttacking;
    protected MonsterType monsterType = MonsterType.GROUND;
    protected MonsterBehavior behavior;
    protected BufferedImage fallbackSprite;
    protected BufferedImage projectileSprite;
    private int attackCooldownTicks;
    private int attackCooldownRemaining;

    public Monster(float x, float y, int width, int height, Color color, int hp) {
        this(x, y, width, height, color, hp, 6, 1.6f, 1.0f, 64f, 120f, 36f, 8f, MonsterType.GROUND);
    }

    protected Monster(
            float x,
            float y,
            int width,
            int height,
            Color color,
            int hp,
            int damage,
            float moveSpeed,
            float attackSpeed,
            float moveRange,
            float detectRange,
            float attackRange,
            float frameDuration,
            MonsterType monsterType
    ) {
        super(x, y);
        this.width = width;
        this.height = height;
        this.hp = hp;
        this.damage = damage;
        this.moveSpeed = moveSpeed;
        this.attackSpeed = attackSpeed;
        this.moveRange = moveRange;
        this.detectRange = detectRange;
        this.attackRange = attackRange;
        this.frameDuration = Math.max(1f, frameDuration);
        this.monsterType = monsterType;
        this.spawnX = x;
        this.spawnY = y;
        this.fallbackSprite = createFallbackSprite(color, width, height);
        this.attackCooldownTicks = Math.max(1, Math.round(60f / Math.max(0.1f, attackSpeed)));
        loadDefaultAssets();
    }

    protected Monster(
            float x,
            float y,
            int width,
            int height,
            Color color,
            int hp,
            int damage,
            float moveSpeed,
            float attackSpeed,
            float moveRange,
            float detectRange,
            float attackRange,
            float frameDuration,
            MonsterType monsterType,
            String assetBasePath
    ) {
        this(x, y, width, height, color, hp, damage, moveSpeed, attackSpeed, moveRange, detectRange, attackRange, frameDuration, monsterType);
        loadAssets(assetBasePath);
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    public int getHp() {
        return hp;
    }

    public int getDamage() {
        return damage;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }

    public float getDetectRange() {
        return detectRange;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public float getMoveRange() {
        return moveRange;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public Direction getDirection() {
        return direction;
    }

    public MonsterState getCurrentState() {
        return currentState;
    }

    public void setBehavior(MonsterBehavior behavior) {
        this.behavior = behavior;
    }

    public void setSprite(BufferedImage sprite) {
        if (sprite == null) return;

        // replace fallback sprite and update any asset lists that were using the old fallback
        BufferedImage previousFallback = this.fallbackSprite;
        this.fallbackSprite = sprite;

        if (assets != null) {
            for (MonsterState state : MonsterState.values()) {
                List<BufferedImage> frames = assets.get(state);
                if (frames == null || frames.isEmpty()) {
                    List<BufferedImage> newList = new ArrayList<>();
                    newList.add(sprite);
                    assets.put(state, newList);
                    continue;
                }

                for (int i = 0; i < frames.size(); i++) {
                    if (frames.get(i) == previousFallback) {
                        frames.set(i, sprite);
                    }
                }
            }
        }
    }

    public void setProjectileSprite(BufferedImage sprite) {
        this.projectileSprite = sprite;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setDirection(Direction direction) {
        if (direction != null) {
            this.direction = direction;
        }
    }

    public void setState(MonsterState newState) {
        if (newState != null && currentState != newState) {
            currentState = newState;
            currentFrame = 0;
            frameTimer = 0;
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean shouldBeRemoved() {
        if (!isDead || currentState != MonsterState.DEATH) {
            return false;
        }

        List<BufferedImage> frames = assets.get(MonsterState.DEATH);
        return frames != null && !frames.isEmpty() && currentFrame >= frames.size() - 1;
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), width, height);
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    public int update(Player player, CollisionMap collisionMap, List<Bullet> bullets) {
        if (isDead) {
            updateAnimation();
            return 0;
        }

        if (attackCooldownRemaining > 0) {
            attackCooldownRemaining--;
        }

        if (behavior != null) {
            behavior.update(this, player, collisionMap);
        } else {
            move(player, collisionMap);
        }

        int damageDealt = 0;
        if (player != null && !isDead) {
            if (isInAttackRange(player) && attackCooldownRemaining == 0) {
                damageDealt = attack(player);
            }
        }

        updateAnimation();
        return damageDealt;
    }

    public void draw(Graphics2D g) {
        if (g == null || (isDead && currentState != MonsterState.DEATH)) {
            return;
        }

        BufferedImage image = getCurrentImage();
        if (image != null) {
            if (direction == Direction.LEFT) {
                g.drawImage(image, (int) Math.round(x + width), (int) Math.round(y), -width, height, null);
            } else {
                g.drawImage(image, (int) Math.round(x), (int) Math.round(y), width, height, null);
            }
            return;
        }

        g.setColor(colorForState());
        g.fillRect((int) Math.round(x), (int) Math.round(y), width, height);
    }

    protected void move(Player player, CollisionMap collisionMap) {
        if (player == null) {
            setState(MonsterState.IDLE);
            return;
        }

        float dx = player.getX() - x;
        float dy = player.getY() - y;
        float distance = (float) Math.hypot(dx, dy);

        if (distance <= detectRange) {
            if (dx < 0) {
                direction = Direction.LEFT;
            } else if (dx > 0) {
                direction = Direction.RIGHT;
            }
            setState(MonsterState.MOVE);
            x += clampStep(dx, moveSpeed);
            y += clampStep(dy, moveSpeed);
        } else {
            setState(MonsterState.IDLE);
        }
    }

    protected int attack(Player player) {
        isAttacking = true;
        setState(MonsterState.ATTACK);
        attackCooldownRemaining = attackCooldownTicks;
        return damage;
    }

    public boolean takeDamage(int incomingDamage) {
        if (isDead || incomingDamage <= 0) {
            return false;
        }

        hp -= incomingDamage;
        if (hp <= 0) {
            die();
            return true;
        }

        setState(MonsterState.HURT);
        return false;
    }

    protected void die() {
        isDead = true;
        setState(MonsterState.DEATH);
    }

    protected boolean isActionState() {
        return currentState == MonsterState.ATTACK
                || currentState == MonsterState.HURT
                || currentState == MonsterState.DEATH;
    }

    protected boolean isInAttackRange(Player player) {
        if (player == null) {
            return false;
        }

        float monsterCenterX = x + width * 0.5f;
        float monsterCenterY = y + height * 0.5f;
        float playerCenterX = player.getX() + (float) (player.getWidth() * 0.5);
        float playerCenterY = player.getY() + (float) (player.getHeight() * 0.5);

        float centerDistance = (float) Math.hypot(playerCenterX - monsterCenterX, playerCenterY - monsterCenterY);
        float reach = attackRange + (float) ((player.getWidth() + width) * 0.25);
        return centerDistance <= reach;
    }

    protected void updateAnimation() {
        List<BufferedImage> frames = assets.get(currentState);
        if (frames == null || frames.isEmpty()) {
            return;
        }

        float frameStep = Math.max(1f, frameDuration / Math.max(1, frames.size()));
        frameTimer += 1f;
        if (frameTimer < frameStep) {
            return;
        }

        frameTimer -= frameStep;
        currentFrame++;

        if (currentFrame >= frames.size()) {
            if (currentState == MonsterState.ATTACK) {
                isAttacking = false;
                setState(MonsterState.IDLE);
            } else if (currentState == MonsterState.HURT) {
                setState(MonsterState.IDLE);
            } else if (currentState == MonsterState.DEATH) {
                isDead = true;
                currentFrame = frames.size() - 1;
            } else {
                currentFrame = 0;
            }
        }
    }

    protected float distanceTo(float targetX, float targetY) {
        return (float) Math.hypot(targetX - x, targetY - y);
    }

    protected boolean canDetectPlayer(Player player) {
        return player != null && distanceTo(player.getX(), player.getY()) <= detectRange;
    }

    protected boolean isPlayerInAttackRange(Player player) {
        return player != null && distanceTo(player.getX(), player.getY()) <= attackRange;
    }

    protected void moveToward(float targetX, float targetY) {
        moveToward(targetX, targetY, moveSpeed);
    }

    protected void moveToward(float targetX, float targetY, float speed) {
        moveToward(targetX, targetY, speed, 0f);
    }

    protected void moveToward(float targetX, float targetY, float speed, float stopDistance) {
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = (float) Math.hypot(dx, dy);
        if (distance == 0f) {
            return;
        }

        float safeStopDistance = Math.max(0f, stopDistance);
        if (safeStopDistance > 0f) {
            float targetDistance = Math.max(0f, distance - safeStopDistance);
            if (targetDistance == 0f) {
                return;
            }

            targetX = x + (dx / distance) * targetDistance;
            targetY = y + (dy / distance) * targetDistance;
            dx = targetX - x;
            dy = targetY - y;
            distance = (float) Math.hypot(dx, dy);
        }

        float appliedSpeed = Math.max(0f, speed);

        // If we're very close to the target, snap to it to avoid oscillation
        if (distance <= appliedSpeed * 0.5f) {
            x = targetX;
            y = targetY;
            return;
        }

        float stepX = (dx / distance) * appliedSpeed;
        float stepY = (dy / distance) * appliedSpeed;

        // Only update facing direction when there's a meaningful horizontal movement
        if (Math.abs(stepX) > 0.01f) {
            direction = stepX < 0 ? Direction.LEFT : Direction.RIGHT;
        }

        x += stepX;
        y += stepY;
    }

    protected void moveTowardAvoidingSolid(float targetCenterX, float targetCenterY, CollisionMap collisionMap) {
        moveTowardAvoidingSolid(targetCenterX, targetCenterY, collisionMap, moveSpeed, 0f);
    }

    protected void moveTowardAvoidingSolid(float targetCenterX, float targetCenterY, CollisionMap collisionMap, float speed) {
        moveTowardAvoidingSolid(targetCenterX, targetCenterY, collisionMap, speed, 0f);
    }

    protected void moveTowardAvoidingSolid(float targetCenterX, float targetCenterY, CollisionMap collisionMap, float speed, float stopDistance) {
        if (collisionMap == null || !collisionMap.isLoaded()) {
            moveToward(targetCenterX - width * 0.5f, targetCenterY - height * 0.5f, speed, stopDistance);
            return;
        }

        float currentCenterX = x + width * 0.5f;
        float currentCenterY = y + height * 0.5f;
        float dx = targetCenterX - currentCenterX;
        float dy = targetCenterY - currentCenterY;
        float distance = (float) Math.hypot(dx, dy);

        if (distance <= Math.max(0f, stopDistance)) {
            return;
        }

        float desiredDistance = distance - Math.max(0f, stopDistance);
        float desiredCenterX = currentCenterX + (dx / distance) * desiredDistance;
        float desiredCenterY = currentCenterY + (dy / distance) * desiredDistance;

        if (!collisionMap.isSegmentBlocked(currentCenterX, currentCenterY, desiredCenterX, desiredCenterY, width, height)) {
            moveCenterTowardIfOpen(desiredCenterX, desiredCenterY, collisionMap, speed);
            return;
        }

        List<Point> path = PATHFINDER.findPath(
                collisionMap,
                width,
                height,
                currentCenterX,
                currentCenterY,
                desiredCenterX,
                desiredCenterY,
                PATH_MAX_VISITED_NODES
        );

        if (path.isEmpty()) {
            return;
        }

        Point waypoint = path.get(0);
        if (path.size() > 1 && Math.hypot(waypoint.x - currentCenterX, waypoint.y - currentCenterY) < PATH_TILE_SIZE * 0.35) {
            waypoint = path.get(1);
        }

        moveCenterTowardIfOpen(waypoint.x, waypoint.y, collisionMap, speed);
    }

    private void moveCenterTowardIfOpen(float targetCenterX, float targetCenterY, CollisionMap collisionMap, float speed) {
        float currentCenterX = x + width * 0.5f;
        float currentCenterY = y + height * 0.5f;
        float dx = targetCenterX - currentCenterX;
        float dy = targetCenterY - currentCenterY;
        float distance = (float) Math.hypot(dx, dy);

        if (distance == 0f) {
            return;
        }

        float appliedSpeed = Math.max(0f, speed);
        float stepDistance = Math.min(distance, appliedSpeed);
        float nextX = x + (dx / distance) * stepDistance;
        float nextY = y + (dy / distance) * stepDistance;

        if (collisionMap.isAreaSolid(nextX, nextY, width, height)) {
            return;
        }

        if (Math.abs(dx) > 0.01f) {
            direction = dx < 0 ? Direction.LEFT : Direction.RIGHT;
        }

        x = nextX;
        y = nextY;
    }

    protected void moveHorizontallyToward(float targetX) {
        moveHorizontallyToward(targetX, 0f);
    }

    protected void moveHorizontallyToward(float targetX, float targetWidth) {
        float dx = targetX - x;
        if (Math.abs(dx) < 0.5f) {
            return;
        }

        float stopDistance = Math.max(0f, (targetWidth + width) * 0.5f);
        float directionStep = Math.signum(dx);
        float desiredX = targetX;

        if (stopDistance > 0f) {
            if (Math.abs(dx) <= stopDistance) {
                return;
            }

            desiredX -= directionStep * stopDistance;
        }

        float distanceToDesired = Math.abs(desiredX - x);
        if (distanceToDesired <= moveSpeed * 0.5f) {
            x = desiredX;
            direction = directionStep < 0 ? Direction.LEFT : Direction.RIGHT;
            return;
        }

        direction = directionStep < 0 ? Direction.LEFT : Direction.RIGHT;
        x += Math.signum(desiredX - x) * moveSpeed;
    }

    protected float getPlayerStopDistance(Player player) {
        if (player == null) {
            return 0f;
        }

        return (float) ((player.getWidth() + width) * 0.5);
    }

    protected float getPlayerCenterX(Player player) {
        return player == null ? x : player.getX() + (float) (player.getWidth() * 0.5);
    }

    protected float getPlayerCenterY(Player player) {
        return player == null ? y : player.getY() + (float) (player.getHeight() * 0.5);
    }

    protected void moveVerticallyToward(float targetY) {
        float dy = targetY - y;
        if (Math.abs(dy) < 0.5f) {
            return;
        }

        y += Math.signum(dy) * moveSpeed;
    }

    protected float clampStep(float delta, float speed) {
        float step = Math.min(Math.abs(delta), speed);
        return Math.copySign(step, delta);
    }

    protected BufferedImage getCurrentImage() {
        List<BufferedImage> frames = assets.get(currentState);
        if (frames != null && !frames.isEmpty()) {
            int safeIndex = Math.min(currentFrame, frames.size() - 1);
            return frames.get(safeIndex);
        }

        return fallbackSprite;
    }

    protected void loadDefaultAssets() {
        for (MonsterState state : MonsterState.values()) {
            assets.put(state, new ArrayList<>());
        }

        assets.get(MonsterState.IDLE).add(fallbackSprite);
        assets.get(MonsterState.MOVE).add(fallbackSprite);
        assets.get(MonsterState.ATTACK).add(fallbackSprite);
        assets.get(MonsterState.HURT).add(fallbackSprite);
        assets.get(MonsterState.DEATH).add(fallbackSprite);
    }

    protected void loadAssets(String basePath) {
        if (basePath == null || basePath.isBlank()) {
            return;
        }

        for (MonsterState state : MonsterState.values()) {
            assets.put(state, loadFrames(basePath, state));
        }

        if (assets.values().stream().allMatch(List::isEmpty)) {
            loadDefaultAssets();
            return;
        }

        applyInitialAssetSize();
    }

    private void applyInitialAssetSize() {
        List<BufferedImage> idleFrames = assets.get(MonsterState.IDLE);
        if (idleFrames == null || idleFrames.isEmpty()) {
            return;
        }

        BufferedImage initialFrame = idleFrames.get(0);
        if (initialFrame == null || initialFrame == fallbackSprite) {
            return;
        }

        width = initialFrame.getWidth();
        height = initialFrame.getHeight();
    }

    private List<BufferedImage> loadFrames(String basePath, MonsterState state) {
        List<BufferedImage> frames = new ArrayList<>();
        String folder = state.name().toLowerCase();

        for (int index = 0; index < MAX_ANIMATION_FRAMES; index++) {
            String candidate = basePath + "/" + folder + "/" + folder + "_" + index + ".png";
            BufferedImage frame = readImageSilently(candidate);
            if (frame != null) {
                frames.add(frame);
            }
        }

        if (frames.isEmpty()) {
            frames.add(fallbackSprite);
        }

        return frames;
    }

    private BufferedImage readImageSilently(String resourcePath) {
        try (InputStream stream = AssetLoader.open(resourcePath)) {
            if (stream == null) {
                return null;
            }

            return ImageIO.read(stream);
        } catch (IOException e) {
            return null;
        }
    }

    private BufferedImage createFallbackSprite(Color color, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color == null ? new Color(180, 80, 80) : color);
        g.fillRoundRect(0, 0, width, height, 10, 10);
        g.setColor(new Color(20, 20, 20, 100));
        g.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);
        g.dispose();
        return image;
    }

    private Color colorForState() {
        return switch (currentState) {
            case ATTACK -> new Color(235, 120, 90);
            case HURT -> new Color(245, 220, 90);
            case DEATH -> new Color(70, 70, 70);
            case MOVE -> new Color(120, 210, 130);
            case IDLE -> new Color(200, 80, 90);
        };
    }
}
