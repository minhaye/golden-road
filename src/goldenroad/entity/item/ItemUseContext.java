package goldenroad.entity.item;

import goldenroad.entity.player.Player;

public interface ItemUseContext {
    Player player();

    Inventory inventory();

    boolean isCurrentMapClear();

    void advanceToNextMap();
}
