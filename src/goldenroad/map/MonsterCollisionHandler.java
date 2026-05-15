package goldenroad.map;

import goldenroad.entity.Monster;

/**
 * Xử lý va chạm vật lý cho Monster với CollisionMap.
 * Tương tự CollisionHandler (dành cho Player) nhưng làm việc với Monster.
 *
 * Hỗ trợ:
 * - Solid tiles (chặn hoàn toàn)
 * - One-way platform (chỉ đứng từ trên xuống)
 * - Edge detection (ngăn monster rơi khỏi vách nếu cần)
 */
public class MonsterCollisionHandler {

    private static final int TILE_SIZE = 16;

    private final CollisionMap map;

    public MonsterCollisionHandler(CollisionMap map) {
        this.map = map;
    }

    // =========================================================
    // PUBLIC API
    // =========================================================

    /**
     * Di chuyển monster theo (dx, dy), kiểm tra va chạm pixel-by-pixel.
     *
     * @param monster monster cần di chuyển
     * @param dx      displacement ngang (pixel, có thể lẻ)
     * @param dy      displacement dọc (pixel, có thể lẻ)
     */
    public void move(Monster monster, double dx, double dy) {
        moveHorizontal(monster, dx);
        moveVertical(monster, dy);
    }

    // =========================================================
    // HORIZONTAL
    // =========================================================

    private void moveHorizontal(Monster monster, double dx) {
        if (dx == 0)
            return;

        double step = Math.signum(dx);

        for (int i = 0; i < (int) Math.abs(dx); i++) {
            double nextX = monster.getX() + step;

            if (!collides(monster, nextX, monster.getY())) {
                monster.setX(nextX);
                monster.setFacingRight(step > 0);
            } else {
                // Đụng tường ngang → dừng, bật vận tốc ngang
                monster.setVelocityX(0);
                break;
            }
        }
    }

    // =========================================================
    // VERTICAL
    // =========================================================

    private void moveVertical(Monster monster, double dy) {
        monster.setOnGround(false);

        if (dy == 0)
            return;

        double step = Math.signum(dy);

        for (int i = 0; i < (int) Math.abs(dy); i++) {
            double nextY = monster.getY() + step;

            // --- SOLID ---
            if (collides(monster, monster.getX(), nextY)) {
                if (step > 0) {
                    monster.setOnGround(true);
                }
                monster.setVelocityY(0);
                break;
            }

            // --- ONE-WAY (chỉ block khi rơi từ trên xuống) ---
            if (step > 0 && collidesOneWay(monster, monster.getX(), nextY)) {
                double prevBottom = monster.getY() + monster.getHeight();
                double nextBottom = nextY + monster.getHeight();

                if (prevBottom <= nextBottom - step) {
                    int tileY = (int) (nextBottom / TILE_SIZE);
                    monster.setY(tileY * TILE_SIZE - monster.getHeight());
                    monster.setOnGround(true);
                    monster.setVelocityY(0);
                    break;
                }
            }

            monster.setY(nextY);
        }
    }

    // =========================================================
    // EDGE DETECTION
    // =========================================================

    /**
     * Kiểm tra xem phía trước mặt monster (theo hướng di chuyển) có "vách hụt"
     * (edge) không — tức là bước tiếp theo monster sẽ bị rơi.
     * Dùng để AI patrol tự quay đầu trước khi rơi khỏi platform.
     *
     * @param monster     monster cần kiểm tra
     * @param moveRight   true nếu đang đi sang phải
     * @param lookAheadPx số pixel nhìn trước (thường = 2–4)
     * @return true nếu phía trước là vách hụt
     */
    public boolean isEdgeAhead(Monster monster, boolean moveRight, int lookAheadPx) {
        double checkX;
        if (moveRight) {
            checkX = monster.getX() + monster.getWidth() + lookAheadPx;
        } else {
            checkX = monster.getX() - lookAheadPx;
        }

        // Kiểm tra 1 pixel bên dưới chân
        double checkY = monster.getY() + monster.getHeight() + 1;

        // Nếu không có ground bên dưới phía trước → edge
        return !hasSolidBelow((int) checkX, (int) checkY);
    }

    /**
     * Trả về true nếu có solid tile (hoặc one-way tile) tại tọa độ pixel (px, py).
     */
    private boolean hasSolidBelow(int px, int py) {
        return map.isSolid(px, py) || map.isOneWay(px, py);
    }

    // =========================================================
    // AABB CHECKS
    // =========================================================

    private boolean collides(Monster m, double x, double y) {
        int left = (int) x;
        int right = (int) (x + m.getWidth() - 1);
        int top = (int) y;
        int bottom = (int) (y + m.getHeight() - 1);

        for (int tx = left; tx <= right; tx++) {
            for (int ty = top; ty <= bottom; ty++) {
                if (map.isSolid(tx, ty))
                    return true;
            }
        }
        return false;
    }

    private boolean collidesOneWay(Monster m, double x, double y) {
        int left = (int) x;
        int right = (int) (x + m.getWidth() - 1);
        int bottom = (int) (y + m.getHeight() - 1);

        for (int tx = left; tx <= right; tx++) {
            if (map.isOneWay(tx, bottom))
                return true;
        }
        return false;
    }
}