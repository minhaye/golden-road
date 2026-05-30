package goldenroad.entity.item;

import goldenroad.entity.player.Player;

public class HpPotionEffect implements ItemEffect {
    private final int healAmount;

    public HpPotionEffect(int healAmount) {
        this.healAmount = healAmount;
    }

    @Override
    public boolean apply(Player player, Inventory inventory) {
        if (player.getHp() >= player.getMaxHp()) return false;
        player.heal(healAmount);
        return true;
    }

    @Override
    public String getDescription() {
        return "HP Potion — Hoi " + healAmount + " HP";
    }
}
