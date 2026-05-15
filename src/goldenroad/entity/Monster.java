package goldenroad.entity;

import goldenroad.map.CollisionMap;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Monster với hệ thống AI đầy đủ:
 * - Physics (double position, velocity, onGround)
 * - State machine (IDLE, PATROL, CHASE, ATTACK, RETREAT)
 * - Pathfinding (lưu path từ A*)
 * - Backward-compatible với API cũ (getBounds, getColor, getHealth, takeDamage)
 */
public class Monster {

    // =========================================================
    // AI STATES
    // =========================================================

    public enum State {
        /** Đứng yên, chờ chuyển sang PATROL. */
        IDLE,
        /** Di chuyển qua lại giữa hai điểm tuần tra. */
        PATROL,
        /** Phát hiện player, đuổi theo bằng A*. */
        CHASE,
        /** Đủ gần để tấn công player. */
        ATTACK,
        /** Máu thấp, bỏ chạy theo hướng ngược lại player. */
        RETREAT
    }

    // =========================================================
    // CONSTANTS
    // =========================================================

    private static final int TILE_SIZE = 16;
    private static final int ATTACK_COOLDOWN_MAX = 60; // frames (~1s ở 60fps)
    private static final int IDLE_TIMEOUT_MAX = 120; // frames trước khi bắt đầu patrol
    private static final int PATH_UPDATE_INTERVAL = 30; // frames giữa 2 lần tính A*
    private static final double GRAVITY = 0.4;
    private static final double MAX_FALL_SPEED = 8.0;

    // =========================================================
    // PHYSICAL PROPERTIES
    // =========================================================

    private double x, y;
    private final int width, height;
    private final Color color;

    private double velocityX = 0;
    private double velocityY = 0;
    private boolean onGround = false;

    // =========================================================
    // STATS
    // =========================================================

    private int health;
    private final int maxHealth;

    /** Damage mỗi lần tấn công player. */
    private int attackDamage = 1;

    /** Tốc độ di chuyển (pixel/frame). */
    private double speed = 1.2;

    /** Phạm vi phát hiện player (pixel). */
    private float detectionRange = 120f;

    /** Phạm vi tấn công (pixel). */
    private float attackRange = 20f;

    /**
     * Ngưỡng HP để chuyển sang RETREAT (0.0–1.0).
     * Mặc định rút lui khi còn 25% máu.
     */
    private float retreatHealthRatio = 0.25f;

    // =========================================================
    // SPRITE & ANIMATION
    // =========================================================

    private BufferedImage spriteSheet;
    private static final int FRAME_W = 48;
    private static final int FRAME_H = 48;
    private static final int DRAW_OFFSET_X = -12;
    private static final int DRAW_OFFSET_Y = -12;

    private int currentFrame = 0;
    private int animationTick = 0;
    private static final int ANIMATION_SPEED = 3;

    // Animation frame counts cho từng state
    private static final int IDLE_FRAMES = 4;
    private static final int WALK_FRAMES = 8;
    private static final int ATTACK_FRAMES = 3;
    private static final int HURT_FRAMES = 2;
    private static final int DEATH_FRAMES = 2;

    // Animation rows
    private static final int IDLE_ROW = 0;
    private static final int WALK_ROW = 1;
    private static final int HURT_ROW = 2;
    private static final int DEATH_ROW = 3;
    private static final int ATTACK_ROW = 4;

    // =========================================================
    // AI STATE MACHINE
    // =========================================================

    private State state = State.IDLE;
    private int idleTimer = 0;
    private int attackCooldown = 0;

    /** MonsterAI instance cho state machine 5 trạng thái (lazy init). */
    private MonsterAI monsterAI = null;

    // =========================================================
    // PATROL
    // =========================================================

    private double patrolStartX;
    private double patrolEndX;
    private int patrolDirection = 1; // +1 = phải, -1 = trái

    // =========================================================
    // PATHFINDING
    // =========================================================

    /** Danh sách waypoints pixel từ A* (center của từng tile). */
    private List<int[]> currentPath = new ArrayList<>();
    private int pathIndex = 0;
    private int pathUpdateTimer = 0;

    /** true nếu đang hướng mặt sang phải. */
    private boolean facingRight = true;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public Monster(int x, int y, int width, int height, Color color) {
        this(x, y, width, height, color, 3);
    }

    public Monster(int x, int y, int width, int height, Color color, int health) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.health = Math.max(1, health);
        this.maxHealth = this.health;

        // Patrol mặc định ±64px xung quanh vị trí spawn
        this.patrolStartX = x - 64;
        this.patrolEndX = x + 64;

        // Load sprite
        loadSprite();
    }

    // =========================================================
    // PHYSICS
    // =========================================================

    /**
     * Áp dụng gravity mỗi frame (gọi trước MonsterCollisionHandler.move).
     */
    public void applyGravity() {
        if (!onGround) {
            velocityY = Math.min(velocityY + GRAVITY, MAX_FALL_SPEED);
        }
    }

    // =========================================================
    // COMBAT
    // =========================================================

    /**
     * Nhận sát thương.
     * 
     * @return true nếu monster chết sau đòn này
     */
    public boolean takeDamage(int damage) {
        if (damage <= 0 || health <= 0)
            return false;
        health -= damage;
        return health <= 0;
    }

    /**
     * Giảm cooldown tấn công 1 frame.
     */
    public void tickAttackCooldown() {
        if (attackCooldown > 0)
            attackCooldown--;
    }

    /**
     * Kích hoạt cooldown sau khi tấn công.
     */
    public void resetAttackCooldown() {
        attackCooldown = ATTACK_COOLDOWN_MAX;
    }

    public boolean canAttack() {
        return attackCooldown == 0;
    }

    // =========================================================
    // PATROL HELPERS
    // =========================================================

    /**
     * Cập nhật hướng patrol theo vị trí hiện tại.
     * 
     * @return velocityX mới cho patrol
     */
    public double computePatrolVelocity() {
        if (x >= patrolEndX) {
            patrolDirection = -1;
        } else if (x <= patrolStartX) {
            patrolDirection = 1;
        }
        return speed * patrolDirection;
    }

    // =========================================================
    // PATH HELPERS
    // =========================================================

    public void setPath(List<int[]> path) {
        this.currentPath = (path != null) ? path : new ArrayList<>();
        this.pathIndex = 0;
    }

    public void clearPath() {
        currentPath.clear();
        pathIndex = 0;
    }

    /** @return waypoint pixel hiện tại, hoặc null nếu hết path */
    public int[] getCurrentWaypoint() {
        if (pathIndex >= currentPath.size())
            return null;
        return currentPath.get(pathIndex);
    }

    /** Chuyển sang waypoint tiếp theo. */
    public void advanceWaypoint() {
        pathIndex++;
    }

    public boolean isPathFinished() {
        return pathIndex >= currentPath.size();
    }

    public int getPathUpdateTimer() {
        return pathUpdateTimer;
    }

    public void incrementPathUpdateTimer() {
        pathUpdateTimer++;
    }

    public void resetPathUpdateTimer() {
        pathUpdateTimer = 0;
    }

    public int getPathUpdateInterval() {
        return PATH_UPDATE_INTERVAL;
    }

    // =========================================================
    // IDLE TIMER
    // =========================================================

    public int getIdleTimer() {
        return idleTimer;
    }

    public void incrementIdleTimer() {
        idleTimer++;
    }

    public void resetIdleTimer() {
        idleTimer = 0;
    }

    public int getIdleTimeoutMax() {
        return IDLE_TIMEOUT_MAX;
    }

    // =========================================================
    // DETECTION
    // =========================================================

    /**
     * Khoảng cách pixel từ tâm monster đến tâm player.
     */
    public double distanceTo(double px, double py) {
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double dx = px - cx;
        double dy = py - cy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public boolean canDetect(double px, double py) {
        return distanceTo(px, py) <= detectionRange;
    }

    public boolean canAttackTarget(double px, double py) {
        return distanceTo(px, py) <= attackRange;
    }

    public boolean shouldRetreat() {
        return (float) health / maxHealth <= retreatHealthRatio;
    }

    // =========================================================
    // BACKWARD-COMPATIBLE API
    // =========================================================

    /** Trả về Rectangle bounds (backward compat với renderer cũ). */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }

    public Color getColor() {
        return color;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    // =========================================================
    // SPRITE & ANIMATION
    // =========================================================

    private void loadSprite() {
        try {
            var stream = getClass().getResourceAsStream("/assets/monster/Zombie_free.png");
            if (stream == null) {
                System.out.println("Không tìm thấy sprite: /assets/monster/Zombie_free.png");
                return;
            }
            spriteSheet = ImageIO.read(stream);
            System.out.println("Load sprite Monster thành công.");
        } catch (IOException e) {
            System.out.println("Lỗi khi load sprite Monster.");
            e.printStackTrace();
        }
    }

    /**
     * Update animation mỗi frame.
     */
    public void updateAnimation() {
        if (spriteSheet == null)
            return;

        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            currentFrame++;

            int maxFrames = getAnimationFrameCount();
            if (currentFrame >= maxFrames) {
                currentFrame = 0;
            }
        }
    }

    private int getAnimationFrameCount() {
        return switch (state) {
            case IDLE -> IDLE_FRAMES;
            case PATROL -> WALK_FRAMES;
            case CHASE -> WALK_FRAMES;
            case ATTACK -> ATTACK_FRAMES;
            case RETREAT -> WALK_FRAMES;
        };
    }

    private int getAnimationRow() {
        return switch (state) {
            case IDLE -> IDLE_ROW;
            case PATROL -> WALK_ROW;
            case CHASE -> WALK_ROW;
            case ATTACK -> ATTACK_ROW;
            case RETREAT -> WALK_ROW;
        };
    }

    private BufferedImage getCurrentFrame() {
        if (spriteSheet == null)
            return null;

        int frameX = currentFrame * FRAME_W;
        int frameY = getAnimationRow() * FRAME_H;

        if (frameX + FRAME_W > spriteSheet.getWidth() ||
                frameY + FRAME_H > spriteSheet.getHeight()) {
            return null;
        }

        return spriteSheet.getSubimage(frameX, frameY, FRAME_W, FRAME_H);
    }

    /**
     * Vẽ monster với sprite animation.
     */
    public void draw(Graphics2D g) {
        if (spriteSheet == null) {
            // Fallback: vẽ hình chữ nhật màu nếu sprite không load được
            g.setColor(color);
            g.fillRect((int) x, (int) y, width, height);
            return;
        }

        BufferedImage frame = getCurrentFrame();
        if (frame == null)
            return;

        // Vẽ sprite, xoay nếu đang hướng trái
        if (facingRight) {
            g.drawImage(frame, (int) x + DRAW_OFFSET_X, (int) y + DRAW_OFFSET_Y,
                    FRAME_W, FRAME_H, null);
        } else {
            g.drawImage(frame, (int) x + DRAW_OFFSET_X + FRAME_W, (int) y + DRAW_OFFSET_Y,
                    -FRAME_W, FRAME_H, null);
        }
    }

    // =========================================================
    // GETTERS / SETTERS
    // =========================================================

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityX(double vx) {
        this.velocityX = vx;
    }

    public void setVelocityY(double vy) {
        this.velocityY = vy;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean b) {
        this.onGround = b;
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public void setFacingRight(boolean b) {
        this.facingRight = b;
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        this.state = s;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public void setAttackDamage(int d) {
        this.attackDamage = d;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double s) {
        this.speed = s;
    }

    public float getDetectionRange() {
        return detectionRange;
    }

    public void setDetectionRange(float r) {
        this.detectionRange = r;
    }

    public float getAttackRange() {
        return attackRange;
    }

    public void setAttackRange(float r) {
        this.attackRange = r;
    }

    public float getRetreatHealthRatio() {
        return retreatHealthRatio;
    }

    public void setRetreatHealthRatio(float r) {
        this.retreatHealthRatio = r;
    }

    public double getPatrolStartX() {
        return patrolStartX;
    }

    public double getPatrolEndX() {
        return patrolEndX;
    }

    public void setPatrolBounds(double startX, double endX) {
        this.patrolStartX = startX;
        this.patrolEndX = endX;
    }

    // =========================================================
    // MONSTER AI
    // =========================================================

    /**
     * Lazy init: tạo MonsterAI nếu chưa có.
     * (Cần CollisionMap, nên khởi tạo trong GamePanel khi đã load map)
     */
    public void initializeAI(CollisionMap collisionMap) {
        if (this.monsterAI == null) {
            this.monsterAI = new MonsterAI(collisionMap);
        }
    }

    public MonsterAI getMonsterAI() {
        return monsterAI;
    }
}