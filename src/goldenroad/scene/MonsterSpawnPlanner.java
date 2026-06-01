package goldenroad.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import goldenroad.entity.monster.Monster;
import goldenroad.entity.monster.MonsterConfig;
import goldenroad.entity.monster.MonsterFactory;
import goldenroad.entity.monster.MonsterType;

final class MonsterSpawnPlanner {
    private static final MonsterConfig[] AIRBORNE_POOL = new MonsterConfig[] {
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_1", 16, 7, 1.2f, 1.0f, 40, 34, 240f, 340f, 42f, 32f, "/assets/monster/airborne-monster/monster_1"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_2", 18, 8, 1.2f, 1.05f, 40, 34, 260f, 360f, 42f, 32f, "/assets/monster/airborne-monster/monster_2"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_3", 20, 9, 1.3f, 1.1f, 40, 34, 280f, 380f, 42f, 32f, "/assets/monster/airborne-monster/monster_3"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_4", 22, 10, 1.3f, 1.15f, 40, 34, 300f, 400f, 42f, 32f, "/assets/monster/airborne-monster/monster_4"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_5", 24, 11, 1.35f, 1.2f, 40, 34, 320f, 420f, 42f, 32f, "/assets/monster/airborne-monster/monster_5")
    };

    private static final SpawnLane[] AIRBORNE_LANES = new SpawnLane[] {
        new SpawnLane(192, 1472, 1760, 1, 2),
        new SpawnLane(1984, 2256, 1600, 1, 2),
        new SpawnLane(2496, 2672, 1500, 1, 2),
        new SpawnLane(3168, 3872, 1400, 1, 2),
        new SpawnLane(576, 752, 1656, 1, 2),
        new SpawnLane(1440, 1584, 1556, 1, 2),
        new SpawnLane(768, 976, 1400, 1, 2),
        new SpawnLane(192, 1104, 760, 1, 2),
        new SpawnLane(2848, 3184, 760, 1, 2),
        new SpawnLane(2480, 2960, 438, 1, 2),
        new SpawnLane(3040, 3184, 438, 1, 2)
    };

    private final Random random = new Random();

    List<Monster> createPrototypeMonsters() {
        List<Monster> monsters = new ArrayList<>();

        spawnLaneGroup(monsters, AIRBORNE_LANES, AIRBORNE_POOL);

        return monsters;
    }

    private void spawnLaneGroup(List<Monster> monsters, SpawnLane[] lanes, MonsterConfig[] pool) {
        for (SpawnLane lane : lanes) {
            int count = lane.randomCount(random);
            int laneWidth = Math.max(1, lane.endX - lane.startX);
            int step = Math.max(1, laneWidth / (count + 1));

            for (int i = 0; i < count; i++) {
                MonsterConfig config = pool[random.nextInt(pool.length)];
                int monsterWidth = config.width;
                int idealX = lane.startX + step * (i + 1);
                int jitter = Math.max(1, step / 4);
                int x = clamp(idealX + random.nextInt(jitter * 2 + 1) - jitter, lane.startX, lane.endX - monsterWidth);
                int y = clamp(lane.y - (config.height / 2), 0, lane.y);

                Monster monster = MonsterFactory.createFromConfig(config, x, y, lane.startX, lane.endX);
                if (monster != null) {
                    monsters.add(monster);
                }
            }
        }
    }

    private int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }

    private static final class SpawnLane {
        private final int startX;
        private final int endX;
        private final int y;
        private final int minCount;
        private final int maxCount;

        private SpawnLane(int startX, int endX, int y, int minCount, int maxCount) {
            this.startX = startX;
            this.endX = endX;
            this.y = y;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }

        private int randomCount(Random random) {
            if (maxCount <= minCount) {
                return Math.max(1, minCount);
            }
            return minCount + random.nextInt(maxCount - minCount + 1);
        }
    }
}