package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionMap;
import java.util.List;

public interface MonsterBehavior {
    /**
     * Update the monster for one tick. Return damage inflicted to player (if any).
     */
    int update(Monster monster, Player player, CollisionMap map, List<Bullet> bullets);
}
