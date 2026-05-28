package goldenroad.render;

import goldenroad.entity.Player;

public class Camera {

    private double x;
    private double y;

    private double lookAheadX = 0;

    private static final double LOOK_AHEAD_DISTANCE = 120;

    public void update(
        Player player,
        int screenWidth,
        int screenHeight,
        int worldWidth,
        int worldHeight
    ) {

        double halfW = screenWidth / 2.0;
        double halfH = screenHeight / 2.0;

        // ===== LOOK AHEAD =====

        double targetLookAhead = 0;

        if (player.getVelocityX() > 0) {
            targetLookAhead = LOOK_AHEAD_DISTANCE;
        }
        else if (player.getVelocityX() < 0) {
            targetLookAhead = -LOOK_AHEAD_DISTANCE;
        }

        lookAheadX +=
            (targetLookAhead - lookAheadX) * 0.04;

        // ===== TARGET =====

        double targetX =
            player.getX()
            - halfW
            + lookAheadX;

        double targetY =
            player.getY()
            - halfH;

        // ===== SMOOTH =====

        x += (targetX - x) * 0.08;
        y += (targetY - y) * 0.08;

        // ===== CLAMP =====

        x = Math.max(0, x);
        y = Math.max(0, y);

        x = Math.min(x, worldWidth - screenWidth);
        y = Math.min(y, worldHeight - screenHeight);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}