package goldenroad.entity;

import java.util.EnumMap;
import java.util.Map;

public class Inventory {

    private static final int HP_POTION_HEAL = 30;
    private static final int MP_POTION_RESTORE = 40;

    private final Map<Item.ItemType, Integer> counts = new EnumMap<>(Item.ItemType.class);

    public Inventory() {
        for (Item.ItemType type : Item.ItemType.values()) {
            counts.put(type, 0);
        }
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

        switch (type) {
            case HP_POTION -> {
                if (player.getHp() >= player.getMaxHp()) {
                    return false;
                }
                player.heal(HP_POTION_HEAL);
            }
            case MP_POTION -> {
                if (player.getMp() >= player.getMaxMp()) {
                    return false;
                }
                player.restoreMp(MP_POTION_RESTORE);
            }
            case KEY -> {
                // key currently has no gameplay effect, but it is still consumable
            }
            default -> {
                return false;
            }
        }

        counts.put(type, getCount(type) - 1);
        return true;
    }

    public String getDescription(Item.ItemType type) {
        return switch (type) {
            case HP_POTION -> "HP Potion — Hoi 30 HP";
            case MP_POTION -> "MP Potion — Hoi 40 MP";
            case KEY -> "Key — Mo cua khoa";
        };
    }
}
