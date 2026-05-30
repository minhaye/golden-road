package goldenroad.entity.item;

import java.util.EnumMap;
import java.util.Map;

import goldenroad.entity.player.Player;

public class Inventory {

    private final Map<Item.ItemType, Integer> counts = new EnumMap<>(Item.ItemType.class);
    private final Map<Item.ItemType, ItemEffect> effects = new EnumMap<>(Item.ItemType.class);

    public Inventory() {
        for (Item.ItemType type : Item.ItemType.values()) {
            counts.put(type, 0);
        }

        // register default effects
        effects.put(Item.ItemType.HP_POTION, new HpPotionEffect(30));
        effects.put(Item.ItemType.MP_POTION, new MpPotionEffect(50));
        effects.put(Item.ItemType.KEY, new KeyEffect());
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

    public boolean useItem(Item.ItemType type, Player player) {
        if (type == null || getCount(type) <= 0) {
            return false;
        }
        ItemEffect effect = effects.get(type);
        if (effect == null) return false;

        boolean applied = effect.apply(player, this);
        if (applied) {
            counts.put(type, getCount(type) - 1);
        }
        return applied;
    }

    public String getDescription(Item.ItemType type) {
        ItemEffect effect = effects.get(type);
        if (effect != null) return effect.getDescription();
        return "";
    }
}
