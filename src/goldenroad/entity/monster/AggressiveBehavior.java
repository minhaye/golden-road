package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionMap;
import java.util.List;

/**
 * Simple aggressive behavior that currently delegates to the monster's default implementation.
 * This is a stub for incremental refactoring; we'll replace with specialized logic later.
 */
public class AggressiveBehavior implements MonsterBehavior {
    @Override
    public int update(Monster monster, Player player, CollisionMap map, List<Bullet> bullets) {
        return monster.performDefaultUpdate(player, map, bullets);
    }
}
