package goldenroad.entity;

import com.badlogic.gdx.graphics.Color;

public class GroundMonster extends Monster {
    private final double minX;
    private final double maxX;
    private final double fixedY;

    public GroundMonster(
        int x,
        int y,
        int width,
        int height,
        Color color,
        int health,
        double moveSpeed,
        double minX,
        double maxX,
        double shootIntervalSeconds,
        double bulletSpeed,
        int bulletDamage,
        int bulletDiameter,
        Color bulletColor
    ) {
        this(x, y, width, height, color, health, moveSpeed, minX, maxX, shootIntervalSeconds, bulletSpeed, bulletDamage, bulletDiameter, bulletColor, RenderShape.RECTANGLE);
    }

    public GroundMonster(
        int x,
        int y,
        int width,
        int height,
        Color color,
        int health,
        double moveSpeed,
        double minX,
        double maxX,
        double shootIntervalSeconds,
        double bulletSpeed,
        int bulletDamage,
        int bulletDiameter,
        Color bulletColor,
        RenderShape renderShape
    ) {
        super(x, y, width, height, color, health, moveSpeed, shootIntervalSeconds, bulletSpeed, bulletDamage, bulletDiameter, bulletColor, renderShape);
        this.minX = Math.min(minX, maxX);
        this.maxX = Math.max(minX, maxX);
        this.fixedY = y;
        setPosition(clamp(x, this.minX, this.maxX), fixedY);
    }

    @Override
    protected void updateMovement(double playerCenterX, double playerCenterY, double deltaSeconds) {
        double targetX = clamp(playerCenterX - (getWidth() / 2.0), minX, maxX);
        double currentX = getX();
        double maxStep = getMoveSpeed() * deltaSeconds;
        double deltaX = targetX - currentX;

        if (Math.abs(deltaX) <= maxStep) {
            setPosition(targetX, fixedY);
            return;
        }

        double direction = Math.signum(deltaX);
        setPosition(currentX + (direction * maxStep), fixedY);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
