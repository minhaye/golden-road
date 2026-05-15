package goldenroad.entity;

import goldenroad.map.CollisionMap;
import goldenroad.map.MonsterCollisionHandler;
import goldenroad.map.Pathfinder;

import java.util.List;

/**
 * Hệ thống AI cho Monster — State Machine 5 trạng thái.
 *
 * <pre>
 *  ┌──────┐  timeout   ┌────────┐  detect    ┌───────┐
 *  │ IDLE │ ─────────► │ PATROL │ ─────────► │ CHASE │◄──────┐
 *  └──────┘            └────────┘            └───┬───┘       │
 *      ▲                   ▲                     │           │lose
 *      │                   │lose sight           │in range   │sight
 *      │                   └─────────────────────┘           │
 *      │                                     ▼               │
 *      │                               ┌─────────┐           │
 *      │                               │ ATTACK  │ ──────────┘
 *      │                               └────┬────┘
 *      │                                    │ low HP
 *      │                                    ▼
 *      │                              ┌──────────┐
 *      └──────────────────────────────│ RETREAT  │
 *                  (arrive safe zone) └──────────┘
 * </pre>
 *
 * Cách dùng (trong game loop):
 * 
 * <pre>
 * MonsterAI ai = new MonsterAI(collisionMap);
 * // mỗi frame:
 * ai.update(monster, playerX, playerY, playerWidth, playerHeight);
 * </pre>
 *
 * Để nhận event tấn công (deal damage), implement {@link AttackListener} và đặt
 * {@code ai.setAttackListener(listener)}.
 */
public class MonsterAI {

    // =========================================================
    // CALLBACK
    // =========================================================

    /**
     * Listener nhận event khi monster tấn công player.
     * Game cần implement interface này để trừ máu player.
     */
    public interface AttackListener {
        /**
         * @param monster monster đang tấn công
         * @param damage  damage gây ra
         */
        void onMonsterAttack(Monster monster, int damage);
    }

    // =========================================================
    // FIELDS
    // =========================================================

    private final MonsterCollisionHandler collisionHandler;
    private final Pathfinder pathfinder;
    private AttackListener attackListener;

    /** Khoảng cách pixel đến waypoint coi là "đã đến" (threshold). */
    private static final int WAYPOINT_REACH_THRESHOLD = 8;

    /**
     * Khoảng cách pixel tính là "mất tầm nhìn" (xa hơn detectionRange × hệ số này).
     */
    private static final float LOSE_SIGHT_MULTIPLIER = 1.4f;

    /** Tốc độ rút lui (pixel/frame), nhanh hơn patrol một chút. */
    private static final double RETREAT_SPEED_MULTIPLIER = 1.5;

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public MonsterAI(CollisionMap map) {
        this.collisionHandler = new MonsterCollisionHandler(map);
        this.pathfinder = new Pathfinder();
        this.mapRef = map;
    }

    public void setAttackListener(AttackListener listener) {
        this.attackListener = listener;
    }

    // =========================================================
    // MAIN UPDATE (gọi mỗi frame)
    // =========================================================

    /**
     * Cập nhật AI và vật lý monster.
     *
     * @param monster monster cần update
     * @param playerX tọa độ X pixel của player (thường player.getX())
     * @param playerY tọa độ Y pixel của player
     * @param playerW chiều rộng player (để tính tâm)
     * @param playerH chiều cao player
     */
    public void update(Monster monster,
            double playerX, double playerY,
            int playerW, int playerH) {

        // Tâm player
        double px = playerX + playerW / 2.0;
        double py = playerY + playerH / 2.0;

        // --- Physics ---
        monster.applyGravity();
        monster.tickAttackCooldown();

        // --- State machine ---
        switch (monster.getState()) {
            case IDLE -> updateIdle(monster, px, py);
            case PATROL -> updatePatrol(monster, px, py);
            case CHASE -> updateChase(monster, px, py);
            case ATTACK -> updateAttack(monster, px, py);
            case RETREAT -> updateRetreat(monster, px, py);
        }

        // --- Apply velocity ---
        collisionHandler.move(monster,
                monster.getVelocityX(),
                monster.getVelocityY());
    }

    // =========================================================
    // IDLE
    // =========================================================

    private void updateIdle(Monster monster, double px, double py) {
        monster.setVelocityX(0);

        // Phát hiện player → CHASE ngay
        if (monster.canDetect(px, py)) {
            transitionTo(monster, Monster.State.CHASE);
            return;
        }

        // Sau timeout → PATROL
        monster.incrementIdleTimer();
        if (monster.getIdleTimer() >= monster.getIdleTimeoutMax()) {
            monster.resetIdleTimer();
            transitionTo(monster, Monster.State.PATROL);
        }
    }

    // =========================================================
    // PATROL
    // =========================================================

    private void updatePatrol(Monster monster, double px, double py) {
        // Phát hiện player → CHASE
        if (monster.canDetect(px, py)) {
            transitionTo(monster, Monster.State.CHASE);
            return;
        }

        // Kiểm tra máu thấp → RETREAT (vừa bị tấn công khi tuần tra)
        if (monster.shouldRetreat()) {
            transitionTo(monster, Monster.State.RETREAT);
            return;
        }

        // Tính velocity patrol (tự đảo chiều khi chạm biên patrol)
        double vx = monster.computePatrolVelocity();

        // Dừng trước vách hụt (edge detection)
        boolean goingRight = vx > 0;
        if (collisionHandler.isEdgeAhead(monster, goingRight, 2)) {
            // Đảo chiều ngay
            monster.setVelocityX(-vx);
            return;
        }

        monster.setVelocityX(vx);
    }

    // =========================================================
    // CHASE
    // =========================================================

    private void updateChase(Monster monster, double px, double py) {
        // Mất tầm nhìn → IDLE
        double loseSightRange = monster.getDetectionRange() * LOSE_SIGHT_MULTIPLIER;
        if (monster.distanceTo(px, py) > loseSightRange) {
            monster.clearPath();
            transitionTo(monster, Monster.State.IDLE);
            return;
        }

        // Đủ gần → ATTACK
        if (monster.canAttackTarget(px, py)) {
            monster.clearPath();
            transitionTo(monster, Monster.State.ATTACK);
            return;
        }

        // Máu thấp → RETREAT
        if (monster.shouldRetreat()) {
            monster.clearPath();
            transitionTo(monster, Monster.State.RETREAT);
            return;
        }

        // Cập nhật path A* theo interval
        monster.incrementPathUpdateTimer();
        if (monster.isPathFinished()
                || monster.getPathUpdateTimer() >= monster.getPathUpdateInterval()) {

            monster.resetPathUpdateTimer();
            List<int[]> path = pathfinder.findPath(
                    (int) monster.getX(), (int) monster.getY(),
                    (int) (px - monster.getWidth() / 2.0), (int) (py - monster.getHeight() / 2.0),
                    monster.getWidth(), monster.getHeight(),
                    getCollisionMap());
            monster.setPath(path);
        }

        // Di chuyển theo waypoint
        followPath(monster);
    }

    // =========================================================
    // ATTACK
    // =========================================================

    private void updateAttack(Monster monster, double px, double py) {
        monster.setVelocityX(0);

        // Player chạy khỏi attack range → CHASE
        if (!monster.canAttackTarget(px, py)) {
            transitionTo(monster, Monster.State.CHASE);
            return;
        }

        // Máu thấp → RETREAT
        if (monster.shouldRetreat()) {
            transitionTo(monster, Monster.State.RETREAT);
            return;
        }

        // Tấn công nếu hết cooldown
        if (monster.canAttack()) {
            monster.resetAttackCooldown();
            if (attackListener != null) {
                attackListener.onMonsterAttack(monster, monster.getAttackDamage());
            }
        }

        // Hướng mặt về phía player
        monster.setFacingRight(px > monster.getX() + monster.getWidth() / 2.0);
    }

    // =========================================================
    // RETREAT
    // =========================================================

    private void updateRetreat(Monster monster, double px, double py) {
        // Nếu đã đủ xa (2× detection range) → IDLE để hồi phục
        double safeDistance = monster.getDetectionRange() * 2.0;
        if (monster.distanceTo(px, py) >= safeDistance) {
            transitionTo(monster, Monster.State.IDLE);
            return;
        }

        // Chạy ngược hướng player
        double cx = monster.getX() + monster.getWidth() / 2.0;
        double cy = monster.getY() + monster.getHeight() / 2.0;
        double dirX = cx - px; // ngược chiều tới player
        double length = Math.sqrt(dirX * dirX + 1); // normalize 1D

        double retreatSpeed = monster.getSpeed() * RETREAT_SPEED_MULTIPLIER;
        double vx = (dirX / length) * retreatSpeed;

        // Không rơi khỏi edge khi retreat
        boolean goingRight = vx > 0;
        if (collisionHandler.isEdgeAhead(monster, goingRight, 2)) {
            vx = 0; // dừng hơn là rơi
        }

        monster.setVelocityX(vx);
    }

    // =========================================================
    // PATH FOLLOWING
    // =========================================================

    /**
     * Di chuyển monster theo currentPath.
     * Khi đến gần waypoint (< WAYPOINT_REACH_THRESHOLD), chuyển sang waypoint tiếp.
     */
    private void followPath(Monster monster) {
        int[] waypoint = monster.getCurrentWaypoint();
        if (waypoint == null) {
            monster.setVelocityX(0);
            return;
        }

        double wpX = waypoint[0];
        double cx = monster.getX() + monster.getWidth() / 2.0;
        double dx = wpX - cx;

        // Đã đến waypoint → advance
        if (Math.abs(dx) <= WAYPOINT_REACH_THRESHOLD) {
            monster.advanceWaypoint();
            waypoint = monster.getCurrentWaypoint();
            if (waypoint == null) {
                monster.setVelocityX(0);
                return;
            }
            dx = waypoint[0] - cx;
        }

        // Velocity theo hướng waypoint
        double vx = Math.signum(dx) * monster.getSpeed();
        monster.setVelocityX(vx);
        monster.setFacingRight(vx > 0);
    }

    // =========================================================
    // STATE TRANSITION
    // =========================================================

    private void transitionTo(Monster monster, Monster.State newState) {
        monster.setState(newState);

        // Reset khi vào IDLE
        if (newState == Monster.State.IDLE) {
            monster.resetIdleTimer();
            monster.setVelocityX(0);
        }
    }

    // =========================================================
    // ACCESSOR (để Pathfinder lấy map)
    // =========================================================

    /**
     * Trả về CollisionMap mà CollisionHandler đang dùng.
     * Vì CollisionMap được inject vào trong constructor, ta giữ reference.
     */
    private final CollisionMap mapRef;

    private CollisionMap getCollisionMap() {
        return mapRef;
    }
}