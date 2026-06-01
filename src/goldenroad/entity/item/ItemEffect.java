package goldenroad.entity.item;

public interface ItemEffect {
    ItemUseResult apply(ItemUseContext ctx);

    String getDescription();
}
