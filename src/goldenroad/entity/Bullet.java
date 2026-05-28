package goldenroad.entity;

import goldenroad.map.CollisionMap;
import java.awt.Color;
import java.awt.Rectangle;

public class Bullet {
    private CollisionMap collisionMap;
    private boolean destroyed = false;

    private double x;
    private double y;

    private final double velocityX;
    private final double velocityY;

    private final int diameter;
    private final Color color;
    private final int damage;

    public Bullet(double x, double y, double directionX, double directionY, double speed, int diameter, Color color, int damage, CollisionMap collisionMap) {
        this.x = x;
        this.y = y;
        this.collisionMap = collisionMap;

        double length = Math.hypot(directionX, directionY);
        if (length == 0) {
            directionX = 1;
            directionY = 0;
            length = 1;
        }

        this.velocityX = (directionX / length) * speed;
        this.velocityY = (directionY / length) * speed;
        this.diameter = diameter;
        this.color = color;
        this.damage = damage;
    }

    public void update() {

    double steps =
        Math.max(
            Math.abs(velocityX),
            Math.abs(velocityY)
        );

    double stepX = velocityX / steps;
    double stepY = velocityY / steps;

    for (int i = 0; i < steps; i++) {

        double nextX = x + stepX;
        double nextY = y + stepY;

        if (collides(nextX, nextY)) {

            destroyed = true;
            return;
        }

        x = nextX;
        y = nextY;
    }
}
private boolean collides(double nextX, double nextY) {
    if (collisionMap == null || !collisionMap.isLoaded()) {
        return false;
    }

    int left =
        (int)Math.floor(nextX);

    int right =
        (int)Math.floor(nextX + diameter - 1);

    int top =
        (int)Math.floor(nextY);

    int bottom =
        (int)Math.floor(nextY + diameter - 1);

    for (int tx = left; tx <= right; tx++) {

        for (int ty = top; ty <= bottom; ty++) {

            if (collisionMap.isSolid(tx, ty)) {
                return true;
            }
        }
    }

    return false;
}

public boolean isDestroyed() {
    return destroyed;
}

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), diameter, diameter);
    }

    public int getRenderX() {
        return (int) Math.round(x);
    }

    public int getRenderY() {
        return (int) Math.round(y);
    }

    public int getDiameter() {
        return diameter;
    }

    public Color getColor() {
        return color;
    }

    public int getDamage() {
        return damage;
    }
}
