package goldenroad.entity.player;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import goldenroad.entity.projectile.Bullet;
import goldenroad.entity.projectile.BulletSpec;

public class PlayerAttack {
    private final Player player;

    private int leftCooldown = 0;
    private int rightCooldown = 0;

    private static final int LEFT_SHOOT_DELAY = 18;
    private static final int RIGHT_SHOOT_DELAY = 120;
    private static final int LEFT_SHOOT_MP_COST = 5;
    private static final int RIGHT_SHOOT_MP_COST = 30;

    private static final double LASER_SPEED = 15;
    private static final int LASER_DIAMETER = 10;
    private static final int LASER_DAMAGE = 4;
    private static final Color LASER_COLOR = new Color(255, 90, 80);

    private static final double CLUSTER_BULLET_SPEED = 15;
    private static final int CLUSTER_BULLET_DIAMETER = 6;
    private static final int CLUSTER_BULLET_DAMAGE = 1;
    private static final int CLUSTER_BULLET_COUNT = 10;
    private static final double CLUSTER_SPREAD_DEGREES = 30.0;
    private static final Color CLUSTER_COLOR = new Color(255, 235, 160);

    public PlayerAttack(Player player) {
        this.player = player;
    }

    public List<BulletSpec> tryLeftShoot(double originX, double originY, double targetX, double targetY) {
        List<BulletSpec> out = new ArrayList<>();
        if (leftCooldown > 0) return out;
        if (!player.spendMp(LEFT_SHOOT_MP_COST)) return out;

        double dirX = targetX - originX;
        double dirY = targetY - originY;
        double len = Math.sqrt(dirX*dirX + dirY*dirY);
        if (len == 0) dirX = 1; else { dirX /= len; dirY /= len; }

        out.add(new BulletSpec(originX, originY, dirX, dirY, LASER_SPEED, LASER_DIAMETER, LASER_COLOR, LASER_DAMAGE, Bullet.BulletType.LASER));
        leftCooldown = LEFT_SHOOT_DELAY;
        return out;
    }

    public List<BulletSpec> tryRightShoot(double originX, double originY, double targetX, double targetY) {
        List<BulletSpec> out = new ArrayList<>();
        if (rightCooldown > 0) return out;
        if (!player.spendMp(RIGHT_SHOOT_MP_COST)) return out;

        double baseX = targetX - originX;
        double baseY = targetY - originY;
        if (baseX == 0 && baseY == 0) baseX = 1;

        for (int i = 0; i < CLUSTER_BULLET_COUNT; i++) {
            double randomAngle = Math.toRadians((Math.random() - 0.5) * CLUSTER_SPREAD_DEGREES);
            double cos = Math.cos(randomAngle);
            double sin = Math.sin(randomAngle);
            double dirX = baseX * cos - baseY * sin;
            double dirY = baseX * sin + baseY * cos;
            double len = Math.sqrt(dirX*dirX + dirY*dirY);
            if (len == 0) dirX = 1; else { dirX /= len; dirY /= len; }

            double speed = CLUSTER_BULLET_SPEED + (Math.random() * 4 - 2);
            double spawnOffsetX = (Math.random() - 0.5) * 8;
            double spawnOffsetY = (Math.random() - 0.5) * 8;

            out.add(new BulletSpec(originX + spawnOffsetX, originY + spawnOffsetY, dirX, dirY, speed, CLUSTER_BULLET_DIAMETER, CLUSTER_COLOR, CLUSTER_BULLET_DAMAGE, Bullet.BulletType.SHOTGUN));
        }

        rightCooldown = RIGHT_SHOOT_DELAY;
        return out;
    }

    public void tick() {
        if (leftCooldown > 0) leftCooldown--;
        if (rightCooldown > 0) rightCooldown--;
    }

    public int getLeftCooldown() { return leftCooldown; }
    public int getLeftCooldownMax() { return LEFT_SHOOT_DELAY; }
    public int getRightCooldown() { return rightCooldown; }
    public int getRightCooldownMax() { return RIGHT_SHOOT_DELAY; }

    public void resetCooldowns() {
        leftCooldown = 0;
        rightCooldown = 0;
    }

    public void applyCooldowns(int left, int right) {
        leftCooldown = Math.max(0, left);
        rightCooldown = Math.max(0, right);
    }
}
