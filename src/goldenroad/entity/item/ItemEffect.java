package goldenroad.entity.item;

import goldenroad.entity.player.Player;

public interface ItemEffect {
    boolean apply(Player player, Inventory inventory);
    String getDescription();
}
