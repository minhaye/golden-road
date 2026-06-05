package goldenroad.map;

import goldenroad.entity.item.Item;
import java.util.Objects;

public final class ItemSpawnPoint {
    private final int x;
    private final int y;
    private final Item.ItemType type;

    public ItemSpawnPoint(int x, int y, Item.ItemType type) {
        this.x = x;
        this.y = y;
        this.type = Objects.requireNonNull(type, "type must not be null");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Item.ItemType getType() {
        return type;
    }
}
