package goldenroad.entity.item;

public class KeyEffect implements ItemEffect {
    @Override
    public ItemUseResult apply(ItemUseContext ctx) {
        if (!ctx.isCurrentMapClear()) {
            return ItemUseResult.fail("Con quai chua het, chua the dung Key");
        }

        ctx.advanceToNextMap();
        return ItemUseResult.ok("Da mo cua, chuyen sang map tiep theo");
    }

    @Override
    public String getDescription() {
        return "Key — Mo cua khoa (can het quai)";
    }
}
