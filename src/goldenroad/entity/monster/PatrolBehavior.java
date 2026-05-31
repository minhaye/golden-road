package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionMap;
import java.util.List;

public class PatrolBehavior implements MonsterBehavior {
    private final double leftBoundary;
    private final double rightBoundary;
    private final double speed;

    private double direction = 1;

    public PatrolBehavior(double leftBoundary, double rightBoundary, double speed) {
        this.leftBoundary = Math.min(leftBoundary, rightBoundary);
        this.rightBoundary = Math.max(leftBoundary, rightBoundary);
        this.speed = speed;
    }

    @Override
    public int update(Monster monster, Player player, CollisionMap map, List<Bullet> bullets) {
        if (monster == null || map == null) {
            return 0;
        }

        if (player != null) {
            double distanceToPlayer = Math.hypot(
                (player.getX() + player.getWidth() / 2.0) - (monster.getX() + monster.getBounds().width / 2.0),
                (player.getY() + player.getHeight() / 2.0) - (monster.getY() + monster.getBounds().height / 2.0)
            );

            if (distanceToPlayer <= monster.getDetectionRange()) {
                return monster.performDefaultUpdate(player, map, bullets);
            }
        }

        double monsterLeft = monster.getX();
        double monsterRight = monster.getX() + monster.getMonsterWidth();

        if (monsterLeft <= leftBoundary) {
            direction = 1;
        } else if (monsterRight >= rightBoundary) {
            direction = -1;
        }

        monster.setFacingDirection(direction);
        boolean moved = monster.moveHorizontal(direction * speed, map, true);
        if (!moved) {
            direction *= -1;
            monster.setFacingDirection(direction);
            monster.moveHorizontal(direction * speed, map, true);
        }

        monster.applyVerticalPhysics(map);
        return 0;
    }
}
