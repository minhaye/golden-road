package goldenroad.entity.item;

import goldenroad.entity.player.Player;
import goldenroad.map.MapId;

public interface ItemUseContext {
    Player player();

    Inventory inventory();

    boolean isCurrentMapClear();

    MapId getCurrentMapId();

    void advanceToNextMap();
}
