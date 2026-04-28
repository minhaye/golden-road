package goldenroad.map;

import goldenroad.entity.Player;

public class CollisionHandler {

    private CollisionMap map;
    private double scale;

    public CollisionHandler(CollisionMap map, double scale) {
        this.map = map;
        this.scale = scale;
    }

    public void move(Player player, double dx, double dy) {

        // ===== HORIZONTAL =====
        moveHorizontal(player, dx);

        // ===== VERTICAL =====
        moveVertical(player, dy);
    }

    private void moveHorizontal(Player player, double dx) {

        double step = Math.signum(dx); // +1 hoặc -1

        for (int i = 0; i < Math.abs(dx); i++) {
            double nextX = player.getX() + step;

            if (!collides(player, nextX, player.getY())) {
                player.setX(nextX);
            } else {
                break; // đụng tường thì dừng
            }
        }
    }

private void moveVertical(Player player, double dy) {

    player.setOnGround(false);

    double step = Math.signum(dy);

    for (int i = 0; i < Math.abs(dy); i++) {

        double nextY = player.getY() + step;

        // ===== SOLID =====
        if (collides(player, player.getX(), nextY)) {

            if (step > 0) {
                player.setOnGround(true);
            }

            player.setVelocityY(0);
            break;
        }

        // ===== ONE WAY =====
        if (step > 0 && collidesOneWay(player, player.getX(), nextY)) {

            double prevBottom = player.getY() + player.getHeight();
            double nextBottom = nextY + player.getHeight();

            // 🔥 CHỈ CHẶN KHI ĐI TỪ TRÊN XUỐNG
            if (prevBottom <= nextBottom - step) {

                player.setOnGround(true);
                player.setVelocityY(0);

                // snap lên platform
                int tileY = (int)(nextBottom / scale);
                player.setY(tileY * scale - player.getHeight());

                break;
            }
        }

        // ===== MOVE =====
        player.setY(nextY);
    }
}
    private boolean collidesOneWay(Player p, double x, double y) {

    int left   = (int)(x / scale);
    int right  = (int)((x + p.getWidth() - 1) / scale);
    int bottom = (int)((y + p.getHeight() - 1) / scale);

    for (int tx = left ; tx <= right; tx++) {
        if (map.isOneWay(tx, bottom) ) {
            return true;
        }
    }

    return false;
    }

    // ===== AABB CHECK =====
    private boolean collides(Player p, double x, double y) {

        int left   = (int)(x / scale);
        int right  = (int)((x + p.getWidth() - 1) / scale);
        int top    = (int)(y / scale);
        int bottom = (int)((y + p.getHeight() - 1) / scale);

        for (int tx = left; tx <= right; tx++) {
            for (int ty = top; ty <= bottom; ty++) {
                if (map.isSolid(tx, ty)) {
                    return true;
                }
            }
        }

        return false;
    }
}