package goldenroad.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class Bullet {
    private double x;
    private double y;

    private final double velocityX;
    private final double velocityY;

    private final int diameter;
    private final Color color;
    private final int damage;

    public Bullet(double x, double y, double directionX, double directionY, double speed, int diameter, Color color, int damage) {
        this.x = x;
        this.y = y;

        double length = Math.hypot(directionX, directionY);
        if (length == 0) {
            directionX = 1;
            directionY = 0;
            length = 1;
        }

        this.velocityX = (directionX / length) * speed;
        this.velocityY = (directionY / length) * speed;
        this.diameter = diameter;
        this.color = new Color(color);
        this.damage = damage;
    }

    public void update() {
        x += velocityX;
        y += velocityY;
    }

    public Rectangle getBounds() {
        return new Rectangle((float) Math.round(x), (float) Math.round(y), diameter, diameter);
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
