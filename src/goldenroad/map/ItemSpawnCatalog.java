package goldenroad.map;

import goldenroad.entity.item.Item;
import java.util.List;

public final class ItemSpawnCatalog {
    private static final List<ItemSpawnPoint> MAP_0_SPAWNS = List.of(
        new ItemSpawnPoint(3450, 1250, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(3400, 1250, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(2250, 1670, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(2250, 1600, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(1400, 950, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(600, 900, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(3870, 450, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(4000, 900, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(2660, 600, Item.ItemType.KEY)
    );

    private static final List<ItemSpawnPoint> MAP_1_SPAWNS = List.of(
        new ItemSpawnPoint(0550, 1650, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(2200, 1550, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(1550, 1500, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(1500, 1500, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(2050, 2000, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(2150, 2000, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(5000, 1200, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(4250, 1500, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(3100, 0400, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(4900, 0370, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(3750, 1200, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(3800, 1200, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(3850, 1200, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(1970, 880, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(2300, 1250, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(1750, 880, Item.ItemType.KEY)
    );

    private static final List<ItemSpawnPoint> MAP_2_SPAWNS = List.of(
        new ItemSpawnPoint(300, 4400, Item.ItemType.HP_POTION),

        new ItemSpawnPoint(2600, 4550, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(2600, 4600, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(2600, 4650, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(1600, 2800, Item.ItemType.HP_POTION),

        new ItemSpawnPoint(2290, 1680, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(2340, 1680, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(950, 1840, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(950, 1790, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(2400, 1200, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(2150, 1200, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(1320, 1200, Item.ItemType.HP_POTION),

        new ItemSpawnPoint(2590, 300, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(750,320, Item.ItemType.KEY),

        new ItemSpawnPoint(720, 3435, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(720, 3400, Item.ItemType.MP_POTION)
    );

    private static final List<ItemSpawnPoint> MAP_3_SPAWNS = List.of(
        new ItemSpawnPoint(2500, 1730, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(2550, 1730, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(610, 1750, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(2600, 1350, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(1200, 930, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(250, 1330, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(300, 1330, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(190, 110, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(190, 60, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(3300, 730, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(3350, 730, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(3400, 730, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(750, 3100, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(850, 3100, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(2850, 3100, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(3050, 3100, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(3280, 2800, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(3700, 2150, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(3850, 2150, Item.ItemType.MP_POTION),

        new ItemSpawnPoint(5050, 240, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(5100, 240, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(5150, 240, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(5900, 550, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(5400, 1680, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(5950, 1840, Item.ItemType.HP_POTION),
        new ItemSpawnPoint(6000, 1840, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(6050, 1840, Item.ItemType.MP_POTION),
        new ItemSpawnPoint(6100, 1840, Item.ItemType.HP_POTION),

        new ItemSpawnPoint(5256,1080, Item.ItemType.KEY)
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
