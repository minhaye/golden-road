package goldenroad.map;

public final class MapCatalog {
    private static final int TILE_SIZE = 16;

    private static final MapDefinition MAP_0 = new MapDefinition(
        MapId.MAP_0,
        "/assets/map/ROOM_1.png",
        "/assets/map/ROOM_1_HIDDEN.png",
        "/assets/map/ROOM_1_COLLISION.png",
        280 * TILE_SIZE,
        160 * TILE_SIZE,
        400,
        1995
    );

    private static final MapDefinition MAP_1 = new MapDefinition(
        MapId.MAP_1,
        "/assets/map/ROOM_1.png",
        "/assets/map/ROOM_1_HIDDEN.png",
        "/assets/map/ROOM_1_COLLISION.png",
        330 * TILE_SIZE,
        140 * TILE_SIZE,
        400,
        1995
    );

    private static final MapDefinition MAP_2 = new MapDefinition(
        MapId.MAP_2,
        "/assets/map/ROOM_2.png",
        "/assets/map/ROOM_2_HIDDEN.png",
        "/assets/map/ROOM_2_COLLISION.png",
        180 * TILE_SIZE,
        300 * TILE_SIZE,
        400,
        1995
    );

    private static final MapDefinition MAP_3 = new MapDefinition(
        MapId.MAP_3,
        "/assets/map/ROOM_3.png",
        "/assets/map/ROOM_3_HIDDEN.png",
        "/assets/map/ROOM_3_COLLISION.png",
        500 * TILE_SIZE,
        210 * TILE_SIZE,
        400,
        1995
    );

    private MapCatalog() {
    }

    public static MapDefinition get(MapId mapId) {
        if (mapId == MapId.MAP_0) {
            return MAP_0;
        }
        if (mapId == MapId.MAP_1) {
            return MAP_1;
        }
        if (mapId == MapId.MAP_2) {
            return MAP_2;
        }
        return MAP_3;
    }
}