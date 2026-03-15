package goldenroad.entity;

import java.awt.Color;
import java.awt.Rectangle;

public class Item {
    public enum Shape {
        RECTANGLE,
        OVAL
    }

    private final Rectangle bounds;
    private final Color color;
    private final Shape shape;

    public Item(int x, int y, int width, int height, Color color, Shape shape) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = color;
        this.shape = shape;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public Color getColor() {
        return color;
    }

    public Shape getShape() {
        return shape;
    }
}
