package goldenroad.scene;

import java.awt.image.BufferedImage;
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
    private final int worldWidth;
    private final int worldHeight;
    private final List<String> tileRows;
    private final int tileSize;
    private final int[][] tileGids;
    private final BufferedImage tilesetImage;

    public Screen(List<Rectangle> solidBlocks) {
        this(solidBlocks, List.of(), List.of(), 960, 540, List.of(), 0, null, null);
    }

    public Screen(List<Rectangle> solidBlocks, List<Monster> monsters, List<Item> items) {
        this(solidBlocks, monsters, items, 960, 540, List.of(), 0, null, null);
    }

    public Screen(List<Rectangle> solidBlocks, List<Monster> monsters, List<Item> items, int worldWidth, int worldHeight) {
        this(solidBlocks, monsters, items, worldWidth, worldHeight, List.of(), 0, null, null);
    }

    public Screen(
        List<Rectangle> solidBlocks,
        List<Monster> monsters,
        List<Item> items,
        int worldWidth,
        int worldHeight,
        List<String> tileRows,
        int tileSize
    ) {
        this(solidBlocks, monsters, items, worldWidth, worldHeight, tileRows, tileSize, null, null);
    }

    public Screen(
        List<Rectangle> solidBlocks,
        List<Monster> monsters,
        List<Item> items,
        int worldWidth,
        int worldHeight,
        int[][] tileGids,
        int tileSize,
        BufferedImage tilesetImage
    ) {
        this(solidBlocks, monsters, items, worldWidth, worldHeight, List.of(), tileSize, tileGids, tilesetImage);
    }

    private Screen(
        List<Rectangle> solidBlocks,
        List<Monster> monsters,
        List<Item> items,
        int worldWidth,
        int worldHeight,
        List<String> tileRows,
        int tileSize,
        int[][] tileGids,
        BufferedImage tilesetImage
    ) {
        this.solidBlocks = new ArrayList<>(solidBlocks);
        this.monsters = new ArrayList<>(monsters);
        this.items = new ArrayList<>(items);
        this.worldWidth = Math.max(960, worldWidth);
        this.worldHeight = Math.max(540, worldHeight);
        this.tileRows = List.copyOf(tileRows);
        this.tileSize = Math.max(0, tileSize);
        this.tileGids = copyTileGids(tileGids);
        this.tilesetImage = tilesetImage;
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

    public BufferedImage getTilesetImage() {
        return tilesetImage;
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
