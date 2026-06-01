package goldenroad.entity.item;

public class HpPotionEffect implements ItemEffect {
    private final int healAmount;

    public HpPotionEffect(int healAmount) {
        this.healAmount = healAmount;
    }

    @Override
    public ItemUseResult apply(ItemUseContext ctx) {
        if (ctx.player().getHp() >= ctx.player().getMaxHp()) {
            return ItemUseResult.fail("HP da day, khong the dung");
        }
        ctx.player().heal(healAmount);
        return ItemUseResult.ok("Hoi +" + healAmount + " HP");
    }

    @Override
    public String getDescription() {
        return "HP Potion — Hoi " + healAmount + " HP";
    }
}
