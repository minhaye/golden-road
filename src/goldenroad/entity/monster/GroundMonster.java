package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

import java.awt.Color;

public class GroundMonster extends Monster {
    public GroundMonster(MonsterConfig config) {
        super(
                0,
                0,
                config.width,
                config.height,
                new Color(190, 90, 90),
                config.hp,
                config.damage,
                config.moveSpeed,
                config.attackSpeed,
                config.moveRange,
                config.detectRange,
                config.attackRange,
                config.frameDuration,
                MonsterType.GROUND,
                config.assetBasePath
        );
    }

    @Override
    protected void move(Player player, CollisionMap collisionMap) {
        if (player != null && canDetectPlayer(player)) {
            moveHorizontallyToward(player.getX(), (float) player.getWidth());
            setState(MonsterState.MOVE);
            return;
        }

        float leftBound = spawnX - moveRange;
        float rightBound = spawnX + moveRange;

        if (x <= leftBound) {
            direction = Direction.RIGHT;
        } else if (x >= rightBound) {
            direction = Direction.LEFT;
        }

        float speed = getMoveSpeed();
        if (direction == Direction.LEFT) {
            x -= speed;
        } else {
            x += speed;
        }

        setState(MonsterState.MOVE);
    }
}
