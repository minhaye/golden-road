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
import goldenroad.util.AssetLoader;
import java.awt.image.BufferedImage;

public class SceneManager {
    private static final int TILE_SIZE = 16;
    private static final int AIRBORNE_MONSTER_WIDTH = 80;
    private static final int AIRBORNE_MONSTER_HEIGHT = 68;
    private static final int AIRBORNE_MONSTER_COUNT = 25;

    private static final MonsterConfig[] AIRBORNE_POOL = new MonsterConfig[] {
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_1", 16, 7, 1.2f, 1.0f, 40, 34, 240f, 340f, 42f, 32f, "/assets/monster/airborne-monster/monster_1"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_2", 18, 8, 1.2f, 1.05f, 40, 34, 260f, 360f, 42f, 32f, "/assets/monster/airborne-monster/monster_2"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_3", 20, 9, 1.3f, 1.1f, 40, 34, 280f, 380f, 42f, 32f, "/assets/monster/airborne-monster/monster_3"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_4", 22, 10, 1.3f, 1.15f, 40, 34, 300f, 400f, 42f, 32f, "/assets/monster/airborne-monster/monster_4"),
        new MonsterConfig(MonsterType.AIRBORNE, "airborne_monster_5", 24, 11, 1.35f, 1.2f, 40, 34, 320f, 420f, 42f, 32f, "/assets/monster/airborne-monster/monster_5")
    };

    private final List<Floor> floors = new ArrayList<>();
    private final MonsterSpawnPlanner monsterSpawnPlanner = new MonsterSpawnPlanner();

    private static final int ITEM_WIDTH = 12;
    private static final int ITEM_HEIGHT = 24;

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
            List.of(
                Item.ofType(280, 355, ITEM_WIDTH, ITEM_HEIGHT, Item.ItemType.HP_POTION),
                Item.ofType(320, 355, ITEM_WIDTH, ITEM_HEIGHT, Item.ItemType.MP_POTION),
                Item.ofType(360, 355, ITEM_WIDTH, ITEM_HEIGHT, Item.ItemType.KEY)
            )
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

    public void spawnRandomItems(int count, int worldWidth, int worldHeight) {
        if (count <= 0) return;
        Random rnd = new Random();
        Screen screen = getCurrentScreen();

        Item.ItemType[] types = Item.ItemType.values();

        for (int i = 0; i < count; i++) {
            int x = rnd.nextInt(Math.max(1, worldWidth - ITEM_WIDTH));
            int y = rnd.nextInt(Math.max(1, worldHeight - ITEM_HEIGHT));

            Item.ItemType type = types[rnd.nextInt(types.length)];
            Item item = Item.ofType(x, y, ITEM_WIDTH, ITEM_HEIGHT, type);
            screen.addItem(item);
        }
    }

    // Spawn up to `count` monsters distributed evenly across the map area.
    // This should be called after the map/world dimensions are known.
    public void spawnMonsters(
        int count,
        int worldWidth,
        int worldHeight,
        CollisionMap collisionMap,
        double playerX,
        double playerY,
        double playerWidth,
        double playerHeight
    ) {
        if (count <= 0 || worldWidth <= 0 || worldHeight <= 0) return;

        int cap = Math.min(AIRBORNE_MONSTER_COUNT, count);
        Random rnd = new Random();
        Screen screen = getCurrentScreen();
        GridPathfinder pathfinder = new GridPathfinder(TILE_SIZE);
        double playerCenterX = playerX + playerWidth / 2.0;
        double playerCenterY = playerY + playerHeight / 2.0;

        // Clear existing monsters (remove all by creating a fresh list in Screen is not exposed,
        // so remove by iterating current monsters)
        List<Monster> existing = new ArrayList<>(screen.getMonsters());
        for (Monster m : existing) {
            screen.removeMonster(m);
        }

        MonsterConfig[] spawnOrder = new MonsterConfig[AIRBORNE_MONSTER_COUNT];
        for (int i = 0; i < spawnOrder.length; i++) {
            spawnOrder[i] = AIRBORNE_POOL[i % AIRBORNE_POOL.length];
        }

        // Distribute spawn by splitting width into segments
        for (int i = 0; i < cap; i++) {
            Monster monster = null;
            MonsterConfig config = spawnOrder[i];

            for (int attempt = 0; attempt < 32 && monster == null; attempt++) {
                double slotCenter = (double) (i + 1) * worldWidth / (cap + 1);
                int jitterX = Math.max(8, worldWidth / Math.max(10, cap * 4));
                int x = clamp(
                    (int) Math.round(slotCenter + (rnd.nextInt(jitterX * 2 + 1) - jitterX)),
                    0,
                    Math.max(0, worldWidth - config.width)
                );

                int minY = Math.max(0, worldHeight / 10);
                int maxY = Math.max(minY + 1, worldHeight - worldHeight / 10);
                int y = clamp(minY + rnd.nextInt(maxY - minY), 0, Math.max(0, worldHeight - config.height));

                if (!isReachableSpawn(collisionMap, pathfinder, playerCenterX, playerCenterY, x, y, config.width, config.height)) {
                    continue;
                }

                monster = createAirborneMonster(config, x, y, Math.max(0, x - 80), Math.min(worldWidth, x + 80));
            }

            if (monster != null) {
                screen.addMonster(monster);
            }
        }
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
