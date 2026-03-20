package goldenroad.entity;

import java.awt.Color;
import java.awt.Rectangle;

public abstract class Monster {
    public enum RenderShape {
        RECTANGLE,
        OVAL,
        TRIANGLE
    }

    private final Rectangle bounds;
    private final Color color;
    private final RenderShape renderShape;

    private double x;
    private double y;

    private int health;
    private final double moveSpeed;

    private final double shootIntervalSeconds;
    private double shootCooldownSeconds;
    private final double bulletSpeed;
    private final int bulletDamage;
    private final int bulletDiameter;
    private final Color bulletColor;

    protected Monster(
        int x,
        int y,
        int width,
        int height,
        Color color,
        int health,
        double moveSpeed,
        double shootIntervalSeconds,
        double bulletSpeed,
        int bulletDamage,
        int bulletDiameter,
        Color bulletColor
    ) {
        this(x, y, width, height, color, health, moveSpeed, shootIntervalSeconds, bulletSpeed, bulletDamage, bulletDiameter, bulletColor, RenderShape.RECTANGLE);
    }

    protected Monster(
        int x,
        int y,
        int width,
        int height,
        Color color,
        int health,
        double moveSpeed,
        double shootIntervalSeconds,
        double bulletSpeed,
        int bulletDamage,
        int bulletDiameter,
        Color bulletColor,
        RenderShape renderShape
    ) {
        this.bounds = new Rectangle(x, y, width, height);
        this.x = x;
        this.y = y;
        this.color = color;
        this.renderShape = renderShape;
        this.health = Math.max(1, health);
        this.moveSpeed = Math.max(0, moveSpeed);

        this.shootIntervalSeconds = Math.max(0.1, shootIntervalSeconds);
        this.shootCooldownSeconds = this.shootIntervalSeconds;
        this.bulletSpeed = Math.max(1.0, bulletSpeed);
        this.bulletDamage = Math.max(1, bulletDamage);
        this.bulletDiameter = Math.max(2, bulletDiameter);
        this.bulletColor = bulletColor;
    }

    public final void update(double playerCenterX, double playerCenterY, double deltaSeconds) {
        updateMovement(playerCenterX, playerCenterY, deltaSeconds);
        shootCooldownSeconds -= deltaSeconds;
    }

    protected abstract void updateMovement(double playerCenterX, double playerCenterY, double deltaSeconds);

    public final Bullet createShotTowardPlayer(double playerCenterX, double playerCenterY) {
        if (!isReadyToShoot()) {
            return null;
        }

        shootCooldownSeconds = shootIntervalSeconds;

        double originX = getCenterX();
        double originY = getCenterY();
        double directionX = playerCenterX - originX;
        double directionY = playerCenterY - originY;

        return new Bullet(
            originX - (bulletDiameter / 2.0),
            originY - (bulletDiameter / 2.0),
            directionX,
            directionY,
            bulletSpeed,
            bulletDiameter,
            bulletColor,
            bulletDamage
        );
    }

    public final boolean isReadyToShoot() {
        return shootCooldownSeconds <= 0;
    }

    public final Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public final Color getColor() {
        return color;
    }

    public final RenderShape getRenderShape() {
        return renderShape;
    }

    public final int getHealth() {
        return health;
    }

    public final boolean takeDamage(int damage) {
        if (damage <= 0 || health <= 0) {
            return false;
        }

        health -= damage;
        return health <= 0;
    }

    protected final double getMoveSpeed() {
        return moveSpeed;
    }

    protected final double getX() {
        return x;
    }

    protected final double getY() {
        return y;
    }

    protected final double getCenterX() {
        return x + (bounds.width / 2.0);
    }

    protected final double getCenterY() {
        return y + (bounds.height / 2.0);
    }

    protected final int getWidth() {
        return bounds.width;
    }

    protected final int getHeight() {
        return bounds.height;
    }

    protected final void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
        bounds.x = (int) Math.round(x);
        bounds.y = (int) Math.round(y);
    }
}
