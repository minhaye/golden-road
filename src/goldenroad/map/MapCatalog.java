package goldenroad.map;

import goldenroad.entity.monster.MonsterType;
import java.util.List;

public final class MapCatalog {
    private static final int TILE_SIZE = 16;

    private static final List<MonsterSpawnPoint> MAP_0_SPAWNS = List.of(
        new MonsterSpawnPoint(2020, 1100, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1500, 950, MonsterType.AIRBORNE, 2),
        new MonsterSpawnPoint(1100, 1200, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(800, 1130, MonsterType.AIRBORNE, 2),
        new MonsterSpawnPoint(250, 1000, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1450, 700, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2200, 700, MonsterType.AIRBORNE, 2),
        new MonsterSpawnPoint(3950, 600, MonsterType.AIRBORNE, 4)
    );

    private static final List<MonsterSpawnPoint> MAP_1_SPAWNS = List.of(
        new MonsterSpawnPoint(2030, 1780, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2400, 1700, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2200, 1500, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(3050, 1700, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(4050, 1550, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(4800, 1500, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(4500, 1250, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(4800, 750, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(4200, 500, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(3700, 500, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(3000, 650, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2500, 570, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2000, 800, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1600, 650, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1000, 650, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(3800, 1150, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(3700, 1080, MonsterType.AIRBORNE, 3)
    );

    private static final List<MonsterSpawnPoint> MAP_2_SPAWNS = List.of(
        new MonsterSpawnPoint(2010, 4350, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1800, 4200, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1800, 3700, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1920, 3200, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2080, 2700, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1500, 2600, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2250, 2250, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1600, 2100, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1800, 1900, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2000, 1800, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1500, 1500, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1350, 1100, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1200, 900, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1900, 900, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2440, 900, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1800, 620, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1500, 620, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2300, 250, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1400, 250, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1000, 250, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(300, 3800, MonsterType.AIRBORNE, 4)

    );

    private static final List<MonsterSpawnPoint> MAP_3_SPAWNS = List.of(
        new MonsterSpawnPoint(1400, 2100, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2500, 2100, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1800, 1900, MonsterType.AIRBORNE, 4),

        new MonsterSpawnPoint(1400, 1800, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(500, 1600, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1600, 1300, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2500, 1300, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2650, 1000, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1800, 900, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2000, 900, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(350, 1300, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(480, 700, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1680, 700, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2100, 700, MonsterType.AIRBORNE, 3),

        new MonsterSpawnPoint(1600, 2700, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1100, 2700, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1200, 3050, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1350, 3050, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1700, 3050, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1850, 3050, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(2650, 3100, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2950, 2900, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(4000, 3050, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(4250, 2900, MonsterType.AIRBORNE, 4),

        new MonsterSpawnPoint(3700, 2100, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(3500, 1900, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(4550, 1400, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(3900, 900, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(4550, 600, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(4850, 500, MonsterType.AIRBORNE, 3),

        new MonsterSpawnPoint(5550, 820, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(6050, 820, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(6250, 1200, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(5850, 1500, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(5200, 1330, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(5350, 1150, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(6200, 2100, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(6500, 2080, MonsterType.AIRBORNE, 4),

        new MonsterSpawnPoint(5400, 2580, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(5550, 2500, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(5600, 2530, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(5750, 2520, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(6000, 2580, MonsterType.AIRBORNE, 4),

        
        new MonsterSpawnPoint(1100, 180, MonsterType.AIRBORNE, 3),
        new MonsterSpawnPoint(1600, 150, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(2320, 50, MonsterType.AIRBORNE, 4)
 
    );

    private static final MapDefinition MAP_0 = new MapDefinition(
        MapId.MAP_0,
        "/assets/map/ROOM_0.png",
        null,
        "/assets/map/ROOM_0_COLLISION.png",
        "/assets/audio/map0.wav",
        280 * TILE_SIZE,
        160 * TILE_SIZE,
        750,
        2190,
        MAP_0_SPAWNS
    );

    private static final MapDefinition MAP_1 = new MapDefinition(
        MapId.MAP_1,
        "/assets/map/ROOM_1.png",
        "/assets/map/ROOM_1_HIDDEN.png",
        "/assets/map/ROOM_1_COLLISION.png",
        "/assets/audio/map1.wav",
        330 * TILE_SIZE,
        140 * TILE_SIZE,
        400,
        2030,
        MAP_1_SPAWNS
    );

    private static final MapDefinition MAP_2 = new MapDefinition(
        MapId.MAP_2,
        "/assets/map/ROOM_2.png",
        "/assets/map/ROOM_2_HIDDEN.png",
        "/assets/map/ROOM_2_COLLISION.png",
        "/assets/audio/map2.wav",
        180 * TILE_SIZE,
        300 * TILE_SIZE,
        400,
        3995,
        MAP_2_SPAWNS
    );

    private static final MapDefinition MAP_3 = new MapDefinition(
        MapId.MAP_3,
        "/assets/map/ROOM_3.png",
        "/assets/map/ROOM_3_HIDDEN.png",
        "/assets/map/ROOM_3_COLLISION.png",
        "/assets/audio/map3.wav",
        500 * TILE_SIZE,
        210 * TILE_SIZE,
        400,
        1995,
        MAP_3_SPAWNS
    );

    private MapCatalog() {
    }

    public static MapDefinition get(MapId mapId) {
        if (mapId == null) {
            return MAP_0;
        }

        return switch (mapId) {
            case MAP_0 -> MAP_0;
            case MAP_1 -> MAP_1;
            case MAP_2 -> MAP_2;
            case MAP_3 -> MAP_3;
        };
    }
}
