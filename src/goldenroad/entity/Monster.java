package goldenroad.entity;

import java.awt.Color;
import java.awt.Rectangle;

public class Monster {
    private final Rectangle bounds;
    private final Color color;
    private int health;

    public Monster(int x, int y, int width, int height, Color color) {
        this(x, y, width, height, color, 3);
    }

    public Monster(int x, int y, int width, int height, Color color, int health) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = color;
        this.health = Math.max(1, health);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Color getColor() {
        return color;
    }

    public int getHealth() {
        return health;
    }

    public boolean takeDamage(int damage) {
        if (damage <= 0 || health <= 0) {
            return false;
        }

        health -= damage;
        return health <= 0;
    }
}
