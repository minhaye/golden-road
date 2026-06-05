package goldenroad.map;

import goldenroad.entity.monster.MonsterType;
import java.util.List;

public final class MapCatalog {
    private static final int TILE_SIZE = 16;

    private static final List<MonsterSpawnPoint> MAP_0_SPAWNS = List.of(
        new MonsterSpawnPoint(500, 1990, MonsterType.AIRBORNE, 0),
        new MonsterSpawnPoint(800, 1990, MonsterType.AIRBORNE, 2),
        new MonsterSpawnPoint(1200, 1550, MonsterType.AIRBORNE, 1)
    );

    private static final List<MonsterSpawnPoint> MAP_1_SPAWNS = List.of(
        new MonsterSpawnPoint(450, 1800, MonsterType.AIRBORNE, 1),
        new MonsterSpawnPoint(900, 1700, MonsterType.AIRBORNE, 3)
    );

    private static final List<MonsterSpawnPoint> MAP_2_SPAWNS = List.of(
        new MonsterSpawnPoint(300, 3800, MonsterType.AIRBORNE, 0)
    );

    private static final List<MonsterSpawnPoint> MAP_3_SPAWNS = List.of(
        new MonsterSpawnPoint(600, 1950, MonsterType.AIRBORNE, 4),
        new MonsterSpawnPoint(1400, 1900, MonsterType.AIRBORNE, 2)
    );

    private static final MapDefinition MAP_0 = new MapDefinition(
        MapId.MAP_0,
        "/assets/map/ROOM_0.png",
        null,
        "/assets/map/ROOM_0_COLLISION.png",
        "/assets/audio/map0.wav",
        280 * TILE_SIZE,
        160 * TILE_SIZE,
        400,
        1995,
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
        1995,
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
