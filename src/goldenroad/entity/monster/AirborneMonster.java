package goldenroad.entity.monster;

import goldenroad.entity.player.Player;
import goldenroad.map.CollisionMap;

import java.awt.Color;

public class AirborneMonster extends Monster {
    public AirborneMonster(MonsterConfig config) {
        super(
                0,
                0,
                config.width,
                config.height,
                new Color(110, 170, 240),
                config.hp,
                config.damage,
                config.moveSpeed,
                config.attackSpeed,
                config.moveRange,
                config.detectRange,
                config.attackRange,
                config.frameDuration,
                MonsterType.AIRBORNE,
                config.assetBasePath
        );
    }

    @Override
    protected void move(Player player, CollisionMap collisionMap) {
        if (player != null && canDetectPlayer(player)) {
            moveToward(player.getX(), player.getY());
            setState(MonsterState.MOVE);
            return;
        }

        float angle = (float) ((System.nanoTime() / 1_000_000_000.0) % (Math.PI * 2));
        float orbitX = spawnX + (float) Math.cos(angle) * moveRange;
        float orbitY = spawnY + (float) Math.sin(angle * 1.3f) * (moveRange * 0.5f);

        moveToward(orbitX, orbitY);
        setState(MonsterState.MOVE);
    }
}