package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

public interface MonsterBehavior {
    void update(Monster monster, Player player, CollisionMap collisionMap);
}