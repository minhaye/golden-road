package goldenroad.map;

import goldenroad.entity.item.Item;
import java.util.List;

public final class ItemSpawnCatalog {
    private static final List<ItemSpawnPoint> MAP_0_SPAWNS = List.of(
        new ItemSpawnPoint(600, 1900, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(700, 1900, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(850,2190, Item.ItemType.KEY)
    );

    private static final List<ItemSpawnPoint> MAP_1_SPAWNS = List.of(
        new ItemSpawnPoint(500, 1850, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(950, 1700, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(600, 2030, Item.ItemType.KEY)
    );

    private static final List<ItemSpawnPoint> MAP_2_SPAWNS = List.of(
        new ItemSpawnPoint(300, 3850, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(450, 3600, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(600,3995, Item.ItemType.KEY)
    );

    private static final List<ItemSpawnPoint> MAP_3_SPAWNS = List.of(
        new ItemSpawnPoint(650, 1900, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(1450, 1850, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(600,1995, Item.ItemType.KEY)
    );

    private ItemSpawnCatalog() {
    }

    public static List<ItemSpawnPoint> get(MapId mapId) {
        if (mapId == null) {
            return MAP_0_SPAWNS;
        }

        return switch (mapId) {
            case MAP_0 -> MAP_0_SPAWNS;
            case MAP_1 -> MAP_1_SPAWNS;
            case MAP_2 -> MAP_2_SPAWNS;
            case MAP_3 -> MAP_3_SPAWNS;
        };
    }
}
