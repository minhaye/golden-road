package goldenroad.entity;

import java.awt.Color;
import java.awt.Rectangle;

public class Monster {
    private final Rectangle bounds;
    private final Color color;

    public Monster(int x, int y, int width, int height, Color color) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = color;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Color getColor() {
        return color;
    }
}
