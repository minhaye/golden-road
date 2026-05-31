package goldenroad.entity.projectile;

import java.awt.Color;

public class BulletSpec {
    public final double originX, originY;
    public final double dirX, dirY;
    public final double speed;
    public final int diameter;
    public final Color color;
    public final int damage;
    public final Bullet.BulletType type;

    public BulletSpec(double originX, double originY, double dirX, double dirY, double speed, int diameter, Color color, int damage, Bullet.BulletType type) {
        this.originX = originX;
        this.originY = originY;
        this.dirX = dirX;
        this.dirY = dirY;
        this.speed = speed;
        this.diameter = diameter;
        this.color = color;
        this.damage = damage;
        this.type = type;
    }
}
