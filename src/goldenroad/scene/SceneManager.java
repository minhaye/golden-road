package goldenroad.scene;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import goldenroad.entity.item.Item;
import goldenroad.entity.monster.AggressiveBehavior;
import goldenroad.entity.monster.IdleBehavior;
import goldenroad.entity.monster.Monster;
import goldenroad.entity.monster.PatrolBehavior;
import goldenroad.entity.monster.AirborneBehavior;
import goldenroad.entity.monster.MonsterConfig;
import goldenroad.entity.monster.MonsterFactory;
import goldenroad.entity.monster.MonsterType;
import goldenroad.map.CollisionMap;
import goldenroad.map.GridPathfinder;
import goldenroad.map.MonsterSpawnPoint;
import goldenroad.settings.Difficulty;
import goldenroad.settings.GameSaveData;
import goldenroad.util.AssetLoader;
import java.awt.image.BufferedImage;

public class SceneManager {
    private static final int TILE_SIZE = 16;
    private static final int AIRBORNE_MONSTER_WIDTH = 80;
    private static final int AIRBORNE_MONSTER_HEIGHT = 68;
    private static final int AIRBORNE_MONSTER_COUNT = 25;

    private static final MonsterConfig[] AIRBORNE_POOL = new MonsterConfig[] {
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_1", 16, 7, 1.2f, 1.0f, 80, 105, 240f, 340f, 42f, 32f, "/assets/monster/airborne-monster/monster_1"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_2", 18, 8, 1.2f, 1.05f, 64, 64, 260f, 360f, 42f, 32f, "/assets/monster/airborne-monster/monster_2"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_3", 20, 9, 1.3f, 1.1f, 64, 64, 280f, 380f, 42f, 32f, "/assets/monster/airborne-monster/monster_3"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_4", 22, 10, 1.3f, 1.15f, 57, 95, 300f, 400f, 42f, 32f, "/assets/monster/airborne-monster/monster_4"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_5", 24, 11, 1.35f, 1.2f, 31, 54, 320f, 420f, 42f, 32f, "/assets/monster/airborne-monster/monster_5")
    };

    private final List<Floor> floors = new ArrayList<>();

    private static final int ITEM_MIN_DISTANCE = 80;
    private static final int ITEM_SPAWN_ATTEMPTS = 48;

    private int currentFloorIndex = 0;
    private int currentScreenIndex = 0;

    public SceneManager() {
        setupPrototypeData();
    }

    private void setupPrototypeData() {
        List<Screen> floor1Screens = new ArrayList<>();
        // start with empty monster lists; monsters will be spawned when a map is loaded
        floor1Screens.add(new Screen(
            List.of(
                new Rectangle(0, 5378, 10920, 70),
                new Rectangle(220, 390, 180, 20),
                new Rectangle(560, 340, 170, 20) 
            ),
            List.of(),
            List.of()
        ));

        floors.add(new Floor(floor1Screens));
    }

    private void addMonsterLane(List<Monster> monsters, int startX, int endX, int y) {
        int firstX = startX + (endX - startX) / 3;
        int secondX = startX + (endX - startX) * 2 / 3;

        monsters.add(createBlockMonster(firstX, y));
        monsters.add(createBlockMonster(secondX, y));
    }

    private Monster createBlockMonster(int x, int y) {
        Monster monster = new Monster(
            x,
            y,
            36,
            40,
            new Color(200, 70, 70),
            6
        );

        monster.setBehavior(new AggressiveBehavior());
        // try to assign a default ground sprite
        try {
            BufferedImage img = AssetLoader.loadImage("/assets/monster/ground-monster/monster_1/move/move_0.png");
            if (img != null) monster.setSprite(img);
        } catch (Exception e) {
            // ignore
        }
        return monster;
    }

    private Monster createIdleMonster(int x, int y) {
        Monster monster = new Monster(
            x,
            y,
            36,
            40,
            new Color(130, 130, 130),
            6
        );

        monster.setBehavior(new IdleBehavior());
        return monster;
    }

    private Monster createIdleMonster(int x, int y, BufferedImage sprite) {
        Monster m = createIdleMonster(x, y);
        if (sprite != null) m.setSprite(sprite);
        return m;
    }

    private Monster createPatrolMonster(int x, int y, int leftBoundary, int rightBoundary) {
        Monster monster = new Monster(
            x,
            y,
            36,
            40,
            new Color(175, 175, 175),
            6
        );

        monster.setBehavior(new PatrolBehavior(leftBoundary, rightBoundary, 1.6));
        // try to assign a default ground sprite for patrol monsters
        try {
            BufferedImage img = loadRandomGroundSprite();
            if (img != null) monster.setSprite(img);
        } catch (Exception e) {
            // ignore
        }

        return monster;
    }

    private Monster createPatrolMonster(int x, int y, int leftBoundary, int rightBoundary, BufferedImage sprite) {
        Monster m = createPatrolMonster(x, y, leftBoundary, rightBoundary);
        if (sprite != null) m.setSprite(sprite);
        return m;
    }

    private BufferedImage loadRandomGroundSprite() {
        String[] candidates = new String[] {
            "/assets/monster/ground-monster/monster_1/move/move_0.png",
            "/assets/monster/ground-monster/monster_2/move/move_0.png",
            "/assets/monster/ground-monster/monster_3/move/move_0.png",
            "/assets/monster/ground-monster/monster_4/move/move_0.png",
            "/assets/monster/ground-monster/monster_1/idle/idle_0.png",
            "/assets/monster/ground-monster/monster_2/idle/idle_0.png"
        };

        for (String c : candidates) {
            BufferedImage img = AssetLoader.loadImage(c);
            if (img != null) return img;
        }

        return null;
    }

    private Monster createAirborneMonster(MonsterConfig config, int x, int y, int leftBoundary, int rightBoundary) {
        Monster monster = MonsterFactory.createFromConfig(config, x, y, leftBoundary, rightBoundary);
        monster.setBehavior(new goldenroad.entity.monster.AirborneBehavior(leftBoundary, rightBoundary, 1.35, 220f, 120f, 1.15f, 36f));
        return monster;
    }

    public Screen getCurrentScreen() {
        return floors.get(currentFloorIndex).getScreen(currentScreenIndex);
    }

    public int getCurrentFloorIndex() {
        return currentFloorIndex;
    }

    public int getCurrentScreenIndex() {
        return currentScreenIndex;
    }

    public boolean moveToRightScreen() {
        Floor floor = floors.get(currentFloorIndex);
        int nextScreen = currentScreenIndex + 1;

        if (!floor.hasScreen(nextScreen)) {
            return false;
        }

        currentScreenIndex = nextScreen;
        return true;
    }

    public boolean moveToLeftScreen() {
        Floor floor = floors.get(currentFloorIndex);
        int previousScreen = currentScreenIndex - 1;

        if (!floor.hasScreen(previousScreen)) {
            return false;
        }

        currentScreenIndex = previousScreen;
        return true;
    }

    public boolean isCurrentMapClear() {
        for (Monster monster : getCurrentScreen().getMonsters()) {
            if (monster != null && !monster.isDead()) {
                return false;
            }
        }
        return true;
    }

    public int killAllMonstersOnCurrentMap() {
        List<Monster> monsters = new ArrayList<>(getCurrentScreen().getMonsters());
        int defeatedMonsterCount = 0;
        for (Monster monster : monsters) {
            if (monster == null || monster.isDead()) {
                continue;
            }
            if (monster.takeDamage(Integer.MAX_VALUE / 2)) {
                defeatedMonsterCount++;
            }
        }
        return defeatedMonsterCount;
    }

    public int spawnMapItems(
        int monsterCount,
        int worldWidth,
        int worldHeight,
        CollisionMap collisionMap,
        double playerX,
        double playerY,
        double playerWidth,
        double playerHeight
    ) {
        return spawnMapItems(
            monsterCount,
            worldWidth,
            worldHeight,
            collisionMap,
            playerX,
            playerY,
            playerWidth,
            playerHeight,
            Difficulty.NORMAL
        );
    }

    public int spawnMapItems(
        int monsterCount,
        int worldWidth,
        int worldHeight,
        CollisionMap collisionMap,
        double playerX,
        double playerY,
        double playerWidth,
        double playerHeight,
        Difficulty difficulty
    ) {
        Screen screen = getCurrentScreen();
        screen.clearItems();

        Difficulty safeDifficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        int basePotionCount = Math.min(8, Math.max(2, Math.round(monsterCount * 0.4f)));
        int potionCount = Math.max(1, Math.round(basePotionCount * safeDifficulty.getHpMpItemMultiplier()));
        potionCount = Math.min(10, potionCount);
        List<Item.ItemType> spawnOrder = new ArrayList<>();
        spawnOrder.add(Item.ItemType.KEY);
        for (int i = 0; i < potionCount; i++) {
            spawnOrder.add(Item.ItemType.HP_POTION);
        }
        for (int i = 0; i < potionCount; i++) {
            spawnOrder.add(Item.ItemType.MP_POTION);
        }

        GridPathfinder pathfinder = new GridPathfinder(TILE_SIZE);
        double playerCenterX = playerX + playerWidth / 2.0;
        double playerCenterY = playerY + playerHeight / 2.0;
        List<int[]> placedPositions = new ArrayList<>();
        Random rnd = new Random();
        int spawned = 0;

        for (Item.ItemType type : spawnOrder) {
            Item item = trySpawnItem(
                type,
                worldWidth,
                worldHeight,
                collisionMap,
                pathfinder,
                playerCenterX,
                playerCenterY,
                placedPositions,
                rnd
            );
            if (item != null) {
                screen.addItem(item);
                placedPositions.add(new int[] { item.getBounds().x, item.getBounds().y });
                spawned++;
            }
        }

        return spawned;
    }

    private Item trySpawnItem(
        Item.ItemType type,
        int worldWidth,
        int worldHeight,
        CollisionMap collisionMap,
        GridPathfinder pathfinder,
        double playerCenterX,
        double playerCenterY,
        List<int[]> placedPositions,
        Random rnd
    ) {
        int size = Item.ITEM_SIZE;

        for (int attempt = 0; attempt < ITEM_SPAWN_ATTEMPTS; attempt++) {
            int x = rnd.nextInt(Math.max(1, worldWidth - size));
            int y = rnd.nextInt(Math.max(1, worldHeight - size));

            if (collisionMap != null
                && collisionMap.isLoaded()
                && collisionMap.isAreaSolid(x, y, size, size)) {
                continue;
            }

            if (!isFarEnoughFromOthers(x, y, placedPositions)) {
                continue;
            }

            if (!isReachableSpawn(
                collisionMap,
                pathfinder,
                playerCenterX,
                playerCenterY,
                x,
                y,
                size,
                size
            )) {
                continue;
            }

            return Item.ofType(x, y, type);
        }

        return null;
    }

    private boolean isFarEnoughFromOthers(int x, int y, List<int[]> placedPositions) {
        for (int[] pos : placedPositions) {
            double dx = x - pos[0];
            double dy = y - pos[1];
            if (Math.hypot(dx, dy) < ITEM_MIN_DISTANCE) {
                return false;
            }
        }
        return true;
    }

    public int getLastSpawnedMonsterCount() {
        return lastSpawnedMonsterCount;
    }

    private int lastSpawnedMonsterCount = 0;

    // Spawn up to `count` monsters distributed evenly across the map area.
    // This should be called after the map/world dimensions are known.
    public void spawnMonsters(
        List<MonsterSpawnPoint> spawnPoints,
        CollisionMap collisionMap,
        int worldWidth,
        int worldHeight
    ) {
        if (spawnPoints == null || spawnPoints.isEmpty() || worldWidth <= 0 || worldHeight <= 0) {
            lastSpawnedMonsterCount = 0;
            return;
        }

        lastSpawnedMonsterCount = 0;
        Screen screen = getCurrentScreen();

        // Clear existing monsters (remove all by creating a fresh list in Screen is not exposed,
        // so remove by iterating current monsters)
        List<Monster> existing = new ArrayList<>(screen.getMonsters());
        for (Monster m : existing) {
            screen.removeMonster(m);
        }

        for (MonsterSpawnPoint point : spawnPoints) {
            int index = Math.max(0, Math.min(point.getConfigIndex(), AIRBORNE_POOL.length - 1));
            MonsterConfig config = AIRBORNE_POOL[index];

            int x = clamp(point.getX(), 0, Math.max(0, worldWidth - config.width));
            int y = clamp(point.getY(), 0, Math.max(0, worldHeight - config.height));
            int leftBoundary = Math.max(0, x - 80);
            int rightBoundary = Math.min(worldWidth, x + 80);

            Monster monster = createAirborneMonster(config, x, y, leftBoundary, rightBoundary);
            if (monster != null) {
                monster.setConfigName(config.name);
                screen.addMonster(monster);
                lastSpawnedMonsterCount++;
            }
        }
    }

    public void replaceMonsters(List<GameSaveData.MonsterSnapshot> snapshots, int worldWidth, int worldHeight) {
        Screen screen = getCurrentScreen();
        screen.clearMonsters();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        for (GameSaveData.MonsterSnapshot snapshot : snapshots) {
            Monster monster = createMonsterFromSave(snapshot, worldWidth, worldHeight);
            if (monster != null) {
                screen.addMonster(monster);
            }
        }
    }

    public void replaceItems(List<GameSaveData.ItemSnapshot> snapshots) {
        Screen screen = getCurrentScreen();
        screen.clearItems();
        if (snapshots == null || snapshots.isEmpty()) {
            return;
        }

        for (GameSaveData.ItemSnapshot snapshot : snapshots) {
            if (snapshot == null || snapshot.getType() == null) {
                continue;
            }
            screen.addItem(Item.ofType(snapshot.getX(), snapshot.getY(), snapshot.getType()));
        }
    }

    private Monster createMonsterFromSave(GameSaveData.MonsterSnapshot snapshot, int worldWidth, int worldHeight) {
        if (snapshot == null || snapshot.getConfigName() == null || snapshot.getConfigName().isBlank()) {
            return null;
        }

        MonsterConfig config = findConfigByName(snapshot.getConfigName());
        if (config == null) {
            return null;
        }

        int x = clamp(Math.round(snapshot.getX()), 0, Math.max(0, worldWidth - config.width));
        int y = clamp(Math.round(snapshot.getY()), 0, Math.max(0, worldHeight - config.height));
        int leftBoundary = Math.max(0, x - 80);
        int rightBoundary = Math.min(worldWidth, x + 80);

        Monster monster = createAirborneMonster(config, x, y, leftBoundary, rightBoundary);
        if (monster == null) {
            return null;
        }

        monster.setConfigName(config.name);
        monster.applySnapshot(snapshot);
        return monster;
    }

    private MonsterConfig findConfigByName(String configName) {
        for (MonsterConfig config : AIRBORNE_POOL) {
            if (config.name.equals(configName)) {
                return config;
            }
        }
        return null;
    }

    private boolean isReachableSpawn(
        CollisionMap collisionMap,
        GridPathfinder pathfinder,
        double playerCenterX,
        double playerCenterY,
        int x,
        int y,
        int monsterWidth,
        int monsterHeight
    ) {
        if (collisionMap == null || !collisionMap.isLoaded()) {
            return true;
        }

        if (collisionMap.isAreaSolid(x, y, monsterWidth, monsterHeight)) {
            return false;
        }

        double monsterCenterX = x + monsterWidth / 2.0;
        double monsterCenterY = y + monsterHeight / 2.0;
        if (Math.round(playerCenterX) == Math.round(monsterCenterX) && Math.round(playerCenterY) == Math.round(monsterCenterY)) {
            return false;
        }

        return !pathfinder.findPath(
            collisionMap,
            20,
            50,
            playerCenterX,
            playerCenterY,
            monsterCenterX,
            monsterCenterY,
            12000
        ).isEmpty();
    }

    private int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }
}
