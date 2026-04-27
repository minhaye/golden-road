package goldenroad.render;

public class Camera {

    private double x, y;
    public double getX() { return x; }
    public double getY() { return y; }
    
    public void follow(double targetX, double targetY) {
        x = targetX;
        y += targetY;
    }

    public void apply(java.awt.Graphics2D g2) {
        g2.translate(-x, -y);
    }

    public void reset(java.awt.Graphics2D g2) {
        g2.translate(x, y);
    }

    
}