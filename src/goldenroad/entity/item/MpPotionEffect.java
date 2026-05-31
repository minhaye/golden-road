package goldenroad.entity.item;

import goldenroad.entity.player.Player;

public class MpPotionEffect implements ItemEffect {
    private final int restoreAmount;

    public MpPotionEffect(int restoreAmount) {
        this.restoreAmount = restoreAmount;
    }

    @Override
    public boolean apply(Player player, Inventory inventory) {
        if (player.getMp() >= player.getMaxMp()) return false;
        player.restoreMp(restoreAmount);
        return true;
    }

    @Override
    public String getDescription() {
        return "MP Potion — Hoi " + restoreAmount + " MP";
    }
}
