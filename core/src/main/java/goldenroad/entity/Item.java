package goldenroad.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

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
        this.color = new Color(color);
        this.shape = shape;
    }

    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    public Color getColor() {
        return color;
    }

    public Shape getShape() {
        return shape;
    }
}
