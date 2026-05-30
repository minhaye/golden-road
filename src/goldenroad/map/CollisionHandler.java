package goldenroad.map;

import goldenroad.entity.player.Player;

public class CollisionHandler {
    
    private static final int TILE_SIZE = 16;
    private CollisionMap map;

    public CollisionHandler(CollisionMap map) {
        this.map = map;
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
            if (step > 0 && collidesOneWay(player, player.getX(), nextY) && !player.isDroppingDown()) {

                double prevBottom = player.getY() + player.getHeight();
                double nextBottom = nextY + player.getHeight();

                // chỉ chặn khi rơi từ trên xuống
                if (prevBottom <= nextBottom - step) {
                    // lấy tile chứa chân player
                    int tileY = (int)(nextBottom / TILE_SIZE);

                    // snap player lên mặt platform
                    player.setY(tileY * TILE_SIZE - player.getHeight());

                    player.setOnGround(true);
                    player.setVelocityY(0);

                    break;
                }
            }
            // ===== MOVE =====
            player.setY(nextY);

        }
    }
    
    private boolean collidesOneWay(Player p, double x, double y) {

        int left   = (int)(x);
        int right  = (int)((x + p.getWidth() - 1));
        int bottom = (int)((y + p.getHeight() - 1));

        for (int tx = left ; tx <= right; tx++) {
            if (map.isOneWay(tx, bottom) ) {
                return true;
            }
        }

        return false;
    }

    // ===== AABB CHECK =====
    private boolean collides(Player p, double x, double y) {

        int left   = (int)(x);
        int right  = (int)((x + p.getWidth() - 1));
        int top    = (int)(y);
        int bottom = (int)((y + p.getHeight() - 1) );

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