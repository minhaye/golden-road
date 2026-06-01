package goldenroad.entity.item;

import java.util.EnumMap;
import java.util.Map;

public class Inventory {

    private final Map<Item.ItemType, Integer> counts = new EnumMap<>(Item.ItemType.class);
    private final Map<Item.ItemType, ItemEffect> effects = new EnumMap<>(Item.ItemType.class);
    private ItemUseContext useContext;

    public Inventory() {
        for (Item.ItemType type : Item.ItemType.values()) {
            counts.put(type, 0);
        }

        effects.put(Item.ItemType.HP_POTION, new HpPotionEffect(30));
        effects.put(Item.ItemType.MP_POTION, new MpPotionEffect(50));
        effects.put(Item.ItemType.KEY, new KeyEffect());
    }

    public void setUseContext(ItemUseContext useContext) {
        this.useContext = useContext;
    }

    public void addItem(Item.ItemType type, int quantity) {
        if (type == null || quantity <= 0) {
            return;
        }
        counts.merge(type, quantity, Integer::sum);
    }

    public int getCount(Item.ItemType type) {
        return counts.getOrDefault(type, 0);
    }

    public int getTotalCount() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public ItemUseResult useItem(Item.ItemType type) {
        if (type == null || getCount(type) <= 0) {
            return ItemUseResult.fail("Khong co item de dung");
        }
        if (useContext == null) {
            return ItemUseResult.fail("Khong the dung item luc nay");
        }

        ItemEffect effect = effects.get(type);
        if (effect == null) {
            return ItemUseResult.fail("Item khong hop le");
        }

        ItemUseResult result = effect.apply(useContext);
        if (result.success()) {
            counts.put(type, getCount(type) - 1);
        }
        return result;
    }

    public String getDescription(Item.ItemType type) {
        ItemEffect effect = effects.get(type);
        if (effect != null) {
            return effect.getDescription();
        }
        return "";
    }
}
