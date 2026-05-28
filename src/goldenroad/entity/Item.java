package goldenroad.entity;

import java.awt.Color;
import java.awt.Rectangle;

public class Item {
    public enum Shape {
        RECTANGLE,
        OVAL
    }

    public enum ItemType {
        HP_POTION,
        MP_POTION,
        KEY
    }

    private final Rectangle bounds;
    private final Color color;
    private final Shape shape;
    private final ItemType type;
    private boolean collected = false;

    public Item(int x, int y, int width, int height, Color color, Shape shape, ItemType type) {
        this.bounds = new Rectangle(x, y, 60, 48);
        this.color = color;
        this.shape = shape;
        this.type = type;
    }

    public Item(int x, int y, int width, int height, Color color, Shape shape) {
        this(x, y, width, height, color, shape, ItemType.HP_POTION);
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

    public ItemType getType() {
        return type;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
