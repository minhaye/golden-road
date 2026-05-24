package goldenroad.scene;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import goldenroad.entity.Item;
import goldenroad.entity.Monster;

public class Screen {
    private final List<Rectangle> solidBlocks;
    private final List<Monster> monsters;
    private final List<Item> items;

    public Screen(List<Rectangle> solidBlocks) {
        this(solidBlocks, List.of(), List.of());
    }

    public Screen(List<Rectangle> solidBlocks, List<Monster> monsters, List<Item> items) {
        this.solidBlocks = new ArrayList<>(solidBlocks);
        this.monsters = new ArrayList<>(monsters);
        this.items = new ArrayList<>(items);
    }

    public List<Rectangle> getSolidBlocks() {
        return Collections.unmodifiableList(solidBlocks);
    }

    public List<Monster> getMonsters() {
        return Collections.unmodifiableList(monsters);
    }

    public boolean removeMonster(Monster monster) {
        return monsters.remove(monster);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }

    public void addItem(Item item) {
        if (item == null) return;
        items.add(item);
    }
}
