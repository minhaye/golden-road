package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionMap;
import java.util.List;

public class IdleBehavior implements MonsterBehavior {
    @Override
    public int update(Monster monster, Player player, CollisionMap map, List<Bullet> bullets) {
        return monster.performDefaultUpdate(null, map, bullets);
    }
}
