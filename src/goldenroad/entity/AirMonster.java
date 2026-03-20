package goldenroad.entity;

import java.awt.Color;

import goldenroad.entity.Monster.RenderShape;

public class AirMonster extends Monster {
    private final double minX;
    private final double maxX;
    private final double minY;
    private final double maxY;

    public AirMonster(
        int x,
        int y,
        int width,
        int height,
        Color color,
        int health,
        double moveSpeed,
        double minX,
        double maxX,
        double minY,
        double maxY,
        double shootIntervalSeconds,
        double bulletSpeed,
        int bulletDamage,
        int bulletDiameter,
        Color bulletColor
    ) {
        this(
            x,
            y,
            width,
            height,
            color,
            health,
            moveSpeed,
            minX,
            maxX,
            minY,
            maxY,
            shootIntervalSeconds,
            bulletSpeed,
            bulletDamage,
            bulletDiameter,
            bulletColor,
            RenderShape.RECTANGLE
        );
    }

    public AirMonster(
        int x,
        int y,
        int width,
        int height,
        Color color,
        int health,
        double moveSpeed,
        double minX,
        double maxX,
        double minY,
        double maxY,
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
        this.minY = Math.min(minY, maxY);
        this.maxY = Math.max(minY, maxY);

        setPosition(
            clamp(x, this.minX, this.maxX),
            clamp(y, this.minY, this.maxY)
        );
    }

    @Override
    protected void updateMovement(double playerCenterX, double playerCenterY, double deltaSeconds) {
        double targetX = clamp(playerCenterX - (getWidth() / 2.0), minX, maxX);
        double targetY = clamp(playerCenterY - (getHeight() / 2.0), minY, maxY);

        double currentX = getX();
        double currentY = getY();
        double deltaX = targetX - currentX;
        double deltaY = targetY - currentY;

        double distance = Math.hypot(deltaX, deltaY);
        if (distance == 0) {
            return;
        }

        double maxStep = getMoveSpeed() * deltaSeconds;
        if (distance <= maxStep) {
            setPosition(targetX, targetY);
            return;
        }

        double ratio = maxStep / distance;
        setPosition(
            currentX + (deltaX * ratio),
            currentY + (deltaY * ratio)
        );
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
