package goldenroad.entity;

public abstract class Entity {
    protected float x, y;
    public Entity(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() { return x; }
    public float getY() { return y; }

    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }

    // Default lifecycle hooks — override in subclasses as needed
    public void update() { }

    public void render(java.awt.Graphics2D g) { }

    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle(
            (int)Math.round(x),
            (int)Math.round(y),
            (int)Math.round(getWidth()),
            (int)Math.round(getHeight())
        );
    }

    // Default width/height for entities. Subclasses should override when applicable.
    public double getWidth() { return 1.0; }
    public double getHeight() { return 1.0; }

}
