package goldenroad.entity.item;

import goldenroad.entity.player.Player;

public class KeyEffect implements ItemEffect {
    @Override
    public boolean apply(Player player, Inventory inventory) {
        // Key currently has no immediate player-side effect; consuming is allowed
        return true;
    }

    @Override
    public String getDescription() {
        return "Key — Mo cua khoa";
    }
}
