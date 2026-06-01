package goldenroad.entity.item;

import goldenroad.map.MapId;

public class KeyEffect implements ItemEffect {
    @Override
    public ItemUseResult apply(ItemUseContext ctx) {
        if (!ctx.isCurrentMapClear()) {
            return ItemUseResult.fail("Con quai chua het, chua the dung Key");
        }

        ctx.advanceToNextMap();
        MapId mapId = ctx.getCurrentMapId();
        String mapName = mapId == null ? "map moi" : mapId.displayName();
        return ItemUseResult.ok("Dang chuyen sang " + mapName);
    }

    @Override
    public String getDescription() {
        return "Key — Mo cua khoa (can het quai)";
    }
}
