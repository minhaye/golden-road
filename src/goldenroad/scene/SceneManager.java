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

public class SceneManager {
    private final List<Floor> floors = new ArrayList<>();

    private static final int ITEM_WIDTH = 12;
    private static final int ITEM_HEIGHT = 24;

    private int currentFloorIndex = 0;
    private int currentScreenIndex = 0;

    public SceneManager() {
        setupPrototypeData();
    }

    private void setupPrototypeData() {
        List<Screen> floor1Screens = new ArrayList<>();
        List<Monster> monsters = new ArrayList<>();

        monsters.add(createIdleMonster(320, 2040));
        monsters.add(createIdleMonster(2624, 760));
        monsters.add(createPatrolMonster(640, 1400, 560, 976));
        monsters.add(createPatrolMonster(2688, 760, 2480, 2960));

        addMonsterLane(monsters, 192, 1472, 2040);
        addMonsterLane(monsters, 1984, 2256, 2040);
        addMonsterLane(monsters, 2496, 2672, 2040);
        addMonsterLane(monsters, 3168, 3872, 1718);
        addMonsterLane(monsters, 576, 752, 1656);
        addMonsterLane(monsters, 1440, 1584, 1556);
        addMonsterLane(monsters, 768, 976, 1400);
        addMonsterLane(monsters, 192, 1104, 760);
        addMonsterLane(monsters, 2848, 3184, 760);
        addMonsterLane(monsters, 2480, 2960, 438);
        addMonsterLane(monsters, 3040, 3184, 438);

        floor1Screens.add(new Screen(
            List.of(
                new Rectangle(0, 5378, 10920, 70),
                new Rectangle(220, 390, 180, 20),
                new Rectangle(560, 340, 170, 20) 
            ),
            monsters,
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
}
