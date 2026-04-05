package goldenroad.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import com.badlogic.gdx.math.Rectangle;

public class ScreenData {
    private final List<Rectangle> solidBlocks;
    private final List<Monster> monsters;
    private final List<Item> items;
    private final int worldWidth;
    private final int worldHeight;
    private final List<String> tileRows;
    private final int tileSize;
    private final int[][] tileGids;

    public ScreenData(
        List<Rectangle> solidBlocks,
        List<Monster> monsters,
        List<Item> items,
        int worldWidth,
        int worldHeight,
        List<String> tileRows,
        int tileSize,
        int[][] tileGids
    ) {
        this.solidBlocks = new ArrayList<>();
        for (Rectangle rect : solidBlocks) {
            this.solidBlocks.add(new Rectangle(rect));
        }
        this.monsters = new ArrayList<>(monsters);
        this.items = new ArrayList<>(items);
        this.worldWidth = Math.max(960, worldWidth);
        this.worldHeight = Math.max(540, worldHeight);
        this.tileRows = List.copyOf(tileRows);
        this.tileSize = Math.max(0, tileSize);
        this.tileGids = copyTileGids(tileGids);
    }

    public List<Rectangle> getSolidBlocks() {
        List<Rectangle> copy = new ArrayList<>();
        for (Rectangle rect : solidBlocks) {
            copy.add(new Rectangle(rect));
        }
        return copy;
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

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public boolean hasTileMap() {
        return hasTiledTileMap() || hasLegacyTileMap();
    }

    public boolean hasLegacyTileMap() {
        return !tileRows.isEmpty() && tileSize > 0;
    }

    public boolean hasTiledTileMap() {
        return tileGids != null && tileGids.length > 0 && tileSize > 0;
    }

    public List<String> getTileRows() {
        return tileRows;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int[][] getTileGids() {
        return copyTileGids(tileGids);
    }

    private int[][] copyTileGids(int[][] source) {
        if (source == null) {
            return null;
        }

        int[][] copy = new int[source.length][];
        for (int y = 0; y < source.length; y++) {
            if (source[y] == null) {
                copy[y] = null;
                continue;
            }
            copy[y] = source[y].clone();
        }
        return copy;
    }
}
