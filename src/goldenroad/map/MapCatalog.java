package goldenroad.map;

public final class MapCatalog {
    private static final int TILE_SIZE = 16;

    private static final MapDefinition MAP_0 = new MapDefinition(
        MapId.MAP_0,
<<<<<<< HEAD
        "/assets/map/ROOM_1.png",
        "/assets/map/ROOM_1_HIDDEN.png",
        "/assets/map/ROOM_1_COLLISION.png",
=======
        "/assets/map/ROOM_0.png",
        null,
        "/assets/map/ROOM_0_COLLISION.png",
>>>>>>> 89a5f5e9a7ba89e9ab01cb20fad02a2219406844
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
<<<<<<< HEAD
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
=======
        if (mapId == null) {
            return MAP_0;
        }

        return switch (mapId) {
            case MAP_0 -> MAP_0;
            case MAP_1 -> MAP_1;
            case MAP_2 -> MAP_2;
            case MAP_3 -> MAP_3;
        };
>>>>>>> 89a5f5e9a7ba89e9ab01cb20fad02a2219406844
    }
}