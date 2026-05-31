package goldenroad.entity.projectile;

import goldenroad.map.CollisionMap;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Bullet {
    private CollisionMap collisionMap;
    private boolean destroyed = false;
    private final BulletType type;
    private static BufferedImage laserSprite;
    private static BufferedImage shotgunSprite;
    private final BufferedImage instanceSprite;

    private double x;
    private double y;

    private final double velocityX;
    private final double velocityY;

    private final int diameter;
    private final Color color;
    private final int damage;
    private int ageFrames = 0;
    private double travelDistance = 0.0;
    private static final int MAX_AGE_FRAMES = 240;
    private static final double MAX_TRAVEL_DISTANCE = 1800.0;

    public enum BulletType {
        LASER,
        SHOTGUN
    }

    public Bullet(double x, double y, double directionX, double directionY, double speed, int diameter, Color color, int damage, CollisionMap collisionMap, BulletType type) {
        this(x, y, directionX, directionY, speed, diameter, color, damage, collisionMap, type, null);
    }

    public Bullet(double x, double y, double directionX, double directionY, double speed, int diameter, Color color, int damage, CollisionMap collisionMap, BulletType type, BufferedImage instanceSprite) {
        this.x = x;
        this.y = y;
        this.collisionMap = collisionMap;
        this.type = type;
        this.instanceSprite = instanceSprite;

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
    
static {
    try {

        laserSprite = javax.imageio.ImageIO.read(
            Bullet.class.getResourceAsStream(
                "/assets/bullet/bullets.png"
            )
        );

        shotgunSprite = javax.imageio.ImageIO.read(
            Bullet.class.getResourceAsStream(
                "/assets/bullet/shotgun_bullets.png"
            )
        );

    } catch (Exception e) {
        e.printStackTrace();
    }
}
    public BulletType getType() {
        return type;
    }

    public BufferedImage getSprite() {
        if (instanceSprite != null) {
            return instanceSprite;
        }

        if (type == BulletType.SHOTGUN) {
            return shotgunSprite;
        }

        return laserSprite;
}

    public double getAngle() {
        return Math.atan2(velocityY, velocityX);
    }
    public void update() {
    if (destroyed) {
        return;
    }

    ageFrames++;
    if (ageFrames > MAX_AGE_FRAMES || travelDistance > MAX_TRAVEL_DISTANCE) {
        destroyed = true;
        return;
    }

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
        travelDistance += Math.hypot(stepX, stepY);
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

    public void render(java.awt.Graphics2D g) {
        BufferedImage sprite = getSprite();
        if (sprite == null) return;

        int width = sprite.getWidth();
        int height = sprite.getHeight();

        Graphics2D bulletG = (Graphics2D) g.create();

        int drawX = getRenderX() - (width / 2);
        int drawY = getRenderY() - (height / 2);

        bulletG.translate(drawX, drawY);
        bulletG.rotate(getAngle(), width / 2.0, height / 2.0);
        bulletG.drawImage(sprite, 0, 0, null);
        bulletG.dispose();
    }
}
