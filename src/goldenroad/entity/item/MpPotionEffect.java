package goldenroad.entity.item;

public class MpPotionEffect implements ItemEffect {
    private final int restoreAmount;

    public MpPotionEffect(int restoreAmount) {
        this.restoreAmount = restoreAmount;
    }

    @Override
    public ItemUseResult apply(ItemUseContext ctx) {
        if (ctx.player().getMp() >= ctx.player().getMaxMp()) {
            return ItemUseResult.fail("MP da day, khong the dung");
        }
        ctx.player().restoreMp(restoreAmount);
        return ItemUseResult.ok("Hoi +" + restoreAmount + " MP");
    }

    @Override
    public String getDescription() {
        return "MP Potion — Hoi " + restoreAmount + " MP";
    }
}
