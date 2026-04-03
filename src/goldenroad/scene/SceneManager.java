package goldenroad.scene;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import goldenroad.entity.AirMonster;
import goldenroad.entity.GroundMonster;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.entity.Monster.RenderShape;

public class SceneManager {
    private static final int TILE_SIZE = 40;
    private static final String ROOM1_RESOURCE_PATH = "goldenroad/scene/room1.json";
    private static final String ROOM1_FILE_PATH = "src/goldenroad/scene/room1.json";
    private static final String TILESET_RESOURCE_PATH = "goldenroad/scene/tileset.png";
    private static final String TILESET_FILE_PATH = "src/goldenroad/scene/tileset.png";

    private final List<Floor> floors = new ArrayList<>();

    private int currentFloorIndex = 0;
    private int currentScreenIndex = 0;

    public SceneManager() {
        setupPrototypeData();
    }

    private void setupPrototypeData() {
        Screen map1 = createMapOne();
        Screen map2 = createMapTwo();
        Screen map3 = createMapThree();

        floors.add(new Floor(List.of(map1)));
        floors.add(new Floor(List.of(map2)));
        floors.add(new Floor(List.of(map3)));
    }

    private Screen createMapOne() {
        Screen tiledMap = loadTiledScreen(
            ROOM1_RESOURCE_PATH,
            ROOM1_FILE_PATH,
            TILESET_RESOURCE_PATH,
            TILESET_FILE_PATH,
            List.of(
                new GroundMonster(1000, 720, 44, 42, new Color(220, 70, 70), 8, 112, 760, 2040, 2.0, 8.8, 1, 8, new Color(255, 145, 120), RenderShape.OVAL)
            ),
            List.of(
                new Item(560, 560, 18, 18, new Color(80, 210, 120), Item.Shape.OVAL)
            )
        );
        if (tiledMap != null) {
            return tiledMap;
        }

        char[][] grid = createGrid(22, 58, '.');

        fillRect(grid, 0, 20, 58, 2, '#');
        fillRect(grid, 0, 16, 6, 4, '#');

        fillRect(grid, 8, 10, 1, 10, '#');
        fillRect(grid, 22, 10, 1, 10, '#');
        fillRect(grid, 8, 10, 15, 1, '#');
        fillRect(grid, 8, 19, 15, 1, '#');

        fillRect(grid, 22, 16, 10, 1, '=');
        fillRect(grid, 33, 14, 8, 1, '=');
        fillRect(grid, 43, 12, 9, 1, '=');

        placeTile(grid, 23, 15, 'K');
        placeTile(grid, 50, 11, 'E');

        return buildScreenFromGrid(
            grid,
            List.of(
                new GroundMonster(1000, 720, 44, 42, new Color(220, 70, 70), 8, 112, 760, 2040, 2.0, 8.8, 1, 8, new Color(255, 145, 120), RenderShape.OVAL)
            ),
            List.of(
                new Item(560, 560, 18, 18, new Color(80, 210, 120), Item.Shape.OVAL)
            )
        );
    }

    private Screen loadTiledScreen(
        String mapResourcePath,
        String mapFilePath,
        String tilesetResourcePath,
        String tilesetFilePath,
        List<Monster> monsters,
        List<Item> items
    ) {
        String json = readTextFromResourceOrFile(mapResourcePath, mapFilePath);
        if (json == null || json.isBlank()) {
            return null;
        }

        int mapWidth = parseFirstInt(json, "\\\"width\\\"\\s*:\\s*(\\d+)", -1);
        int mapHeight = parseFirstInt(json, "\\\"height\\\"\\s*:\\s*(\\d+)", -1);
        int tileSize = parseFirstInt(json, "\\\"tilewidth\\\"\\s*:\\s*(\\d+)", TILE_SIZE);

        int[] flatLayerData = parseFirstLayerData(json);
        if (mapWidth <= 0 || mapHeight <= 0 || flatLayerData.length != mapWidth * mapHeight) {
            return null;
        }

        int[][] tileGids = new int[mapHeight][mapWidth];
        List<Rectangle> solidBlocks = new ArrayList<>();

        for (int index = 0; index < flatLayerData.length; index++) {
            int y = index / mapWidth;
            int x = index % mapWidth;
            int gid = flatLayerData[index];
            tileGids[y][x] = gid;

            if (gid > 0) {
                solidBlocks.add(new Rectangle(x * tileSize, y * tileSize, tileSize, tileSize));
            }
        }

        BufferedImage tilesetImage = loadImageFromResourceOrFile(tilesetResourcePath, tilesetFilePath);

        return new Screen(
            solidBlocks,
            monsters,
            items,
            mapWidth * tileSize,
            mapHeight * tileSize,
            tileGids,
            tileSize,
            tilesetImage
        );
    }

    private String readTextFromResourceOrFile(String resourcePath, String filePath) {
        ClassLoader classLoader = SceneManager.class.getClassLoader();

        try (InputStream stream = classLoader.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
            // Fall back to local file path.
        }

        Path fallbackPath = Path.of(filePath);
        try {
            if (Files.exists(fallbackPath)) {
                return Files.readString(fallbackPath, StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
            return null;
        }

        return null;
    }

    private BufferedImage loadImageFromResourceOrFile(String resourcePath, String filePath) {
        ClassLoader classLoader = SceneManager.class.getClassLoader();

        try (InputStream stream = classLoader.getResourceAsStream(resourcePath)) {
            if (stream != null) {
                return ImageIO.read(stream);
            }
        } catch (IOException ignored) {
            // Fall back to local file path.
        }

        Path fallbackPath = Path.of(filePath);
        try {
            if (Files.exists(fallbackPath)) {
                return ImageIO.read(fallbackPath.toFile());
            }
        } catch (IOException ignored) {
            return null;
        }

        return null;
    }

    private int parseFirstInt(String content, String regex, int defaultValue) {
        Matcher matcher = Pattern.compile(regex).matcher(content);
        if (!matcher.find()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private int[] parseFirstLayerData(String json) {
        Matcher matcher = Pattern.compile("\\\"data\\\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL).matcher(json);
        if (!matcher.find()) {
            return new int[0];
        }

        String[] rawValues = matcher.group(1).split(",");
        int[] data = new int[rawValues.length];

        for (int i = 0; i < rawValues.length; i++) {
            String value = rawValues[i].trim();
            try {
                data[i] = Integer.parseInt(value);
            } catch (NumberFormatException ignored) {
                data[i] = 0;
            }
        }

        return data;
    }

    private Screen createMapTwo() {
        char[][] grid = createGrid(22, 62, '.');

        fillRect(grid, 0, 20, 62, 2, '#');
        fillRect(grid, 0, 17, 26, 1, '=');
        fillRect(grid, 27, 15, 12, 1, '=');

        fillRect(grid, 16, 9, 1, 8, '#');
        fillRect(grid, 16, 9, 18, 1, '#');
        fillRect(grid, 34, 9, 1, 6, '#');
        fillRect(grid, 22, 12, 1, 5, '#');

        fillRect(grid, 35, 7, 12, 1, '=');
        fillRect(grid, 40, 3, 1, 4, '#');
        fillRect(grid, 46, 3, 1, 4, '#');
        fillRect(grid, 40, 3, 7, 1, '#');

        placeTile(grid, 41, 6, 'E');
        placeTile(grid, 45, 2, 'K');

        return buildScreenFromGrid(
            grid,
            List.of(
                new GroundMonster(760, 640, 42, 40, new Color(230, 85, 65), 8, 120, 100, 2360, 2.2, 9.0, 1, 8, new Color(255, 150, 125), RenderShape.TRIANGLE),
                new AirMonster(1560, 300, 38, 36, new Color(95, 185, 230), 7, 148, 1040, 2460, 80, 420, 2.5, 10.0, 1, 8, new Color(145, 220, 255), RenderShape.RECTANGLE)
            ),
            List.of(
                new Item(1490, 220, 16, 16, new Color(245, 205, 90), Item.Shape.OVAL)
            )
        );
    }

    private Screen createMapThree() {
        char[][] grid = createGrid(22, 64, '.');

        fillRect(grid, 0, 20, 64, 2, '#');
        fillRect(grid, 0, 16, 40, 1, '=');
        fillRect(grid, 10, 12, 12, 1, '=');
        fillRect(grid, 24, 8, 10, 1, '=');

        fillRect(grid, 41, 4, 1, 16, '#');
        fillRect(grid, 58, 4, 1, 16, '#');
        fillRect(grid, 41, 4, 18, 1, '#');
        fillRect(grid, 42, 12, 16, 1, '=');

        fillRect(grid, 50, 16, 1, 4, '#');
        fillRect(grid, 46, 18, 12, 1, '=');
        fillRect(grid, 60, 17, 4, 3, '#');

        placeTile(grid, 44, 11, 'E');
        placeTile(grid, 62, 16, 'B');

        return buildScreenFromGrid(
            grid,
            List.of(
                new GroundMonster(430, 600, 44, 40, new Color(210, 75, 75), 9, 124, 80, 2550, 1.9, 9.4, 1, 8, new Color(255, 150, 135), RenderShape.RECTANGLE),
                new GroundMonster(2340, 720, 36, 36, new Color(175, 80, 230), 8, 120, 1980, 2500, 2.0, 9.2, 1, 8, new Color(240, 180, 255), RenderShape.TRIANGLE),
                new AirMonster(1840, 320, 40, 36, new Color(100, 190, 235), 7, 156, 1440, 2520, 120, 520, 2.4, 10.2, 1, 8, new Color(150, 230, 255), RenderShape.OVAL)
            ),
            List.of(
                new Item(1050, 460, 16, 16, new Color(105, 220, 170), Item.Shape.OVAL),
                new Item(2460, 680, 18, 18, new Color(250, 205, 95), Item.Shape.RECTANGLE)
            )
        );
    }

    private Screen buildScreenFromGrid(char[][] grid, List<Monster> monsters, List<Item> items) {
        List<Rectangle> solidBlocks = new ArrayList<>();
        List<String> tileRows = new ArrayList<>();

        int rows = grid.length;
        int cols = grid[0].length;

        for (int y = 0; y < rows; y++) {
            tileRows.add(new String(grid[y]));

            for (int x = 0; x < cols; x++) {
                if (isSolidTile(grid[y][x])) {
                    solidBlocks.add(new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
            }
        }

        return new Screen(
            solidBlocks,
            monsters,
            items,
            cols * TILE_SIZE,
            rows * TILE_SIZE,
            tileRows,
            TILE_SIZE
        );
    }

    private char[][] createGrid(int rows, int cols, char fillTile) {
        char[][] grid = new char[rows][cols];

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                grid[y][x] = fillTile;
            }
        }

        return grid;
    }

    private void fillRect(char[][] grid, int startX, int startY, int width, int height, char tile) {
        int maxY = Math.min(grid.length, startY + height);
        int maxX = Math.min(grid[0].length, startX + width);

        for (int y = Math.max(0, startY); y < maxY; y++) {
            for (int x = Math.max(0, startX); x < maxX; x++) {
                grid[y][x] = tile;
            }
        }
    }

    private void placeTile(char[][] grid, int x, int y, char tile) {
        if (y < 0 || y >= grid.length || x < 0 || x >= grid[0].length) {
            return;
        }

        grid[y][x] = tile;
    }

    private boolean isSolidTile(char tile) {
        return tile == '#' || tile == '=' || tile == 'B';
    }

    public Screen getCurrentScreen() {
        return floors.get(currentFloorIndex).getScreen(0);
    }

    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    public int getCurrentScreenIndex() {
        return 0;
    }

    public boolean canMoveToRightScreen() {
        return currentFloorIndex + 1 < floors.size();
    }

    public boolean moveToRightScreen() {
        if (!canMoveToRightScreen()) {
            return false;
        }

        currentFloorIndex++;
        currentScreenIndex = 0;
        return true;
    }

    public boolean moveToLeftScreen() {
        if (currentFloorIndex == 0) {
            return false;
        }

        currentFloorIndex--;
        currentScreenIndex = 0;
        return true;
    }
}
