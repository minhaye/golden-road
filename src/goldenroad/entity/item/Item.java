package goldenroad.entity.item;

import java.awt.Color;
import java.awt.Rectangle;

public class Item {
    public enum Shape {
        RECTANGLE,
        OVAL
    }

    public enum ItemType {
        HP_POTION(new Color(80, 210, 120), Shape.OVAL),
        MP_POTION(new Color(80, 140, 220), Shape.OVAL),
        KEY(new Color(230, 190, 70), Shape.RECTANGLE);

        private final Color defaultColor;
        private final Shape defaultShape;

        ItemType(Color defaultColor, Shape defaultShape) {
            this.defaultColor = defaultColor;
            this.defaultShape = defaultShape;
        }

        public Color getDefaultColor() {
            return defaultColor;
        }

        public Shape getDefaultShape() {
            return defaultShape;
        }
    }

    private final Rectangle bounds;
    private final Color color;
    private final Shape shape;
    private final ItemType type;
    private boolean collected = false;

    public Item(int x, int y, int width, int height, Color color, Shape shape, ItemType type) {
        this.bounds = new Rectangle(x, y, width, height);
        this.color = color;
        this.shape = shape;
        this.type = type;
    }

    public static Item ofType(int x, int y, int width, int height, ItemType type) {
        return new Item(
            x,
            y,
            width,
            height,
            type.getDefaultColor(),
            type.getDefaultShape(),
            type
        );
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
