package goldenroad.scene;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import goldenroad.entity.Item;
import goldenroad.entity.Monster;

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
                new Item(280, 355, ITEM_WIDTH, ITEM_HEIGHT, new Color(80, 210, 120), Item.Shape.OVAL, Item.ItemType.HP_POTION),
                new Item(320, 355, ITEM_WIDTH, ITEM_HEIGHT, new Color(80, 140, 220), Item.Shape.OVAL, Item.ItemType.MP_POTION),
                new Item(360, 355, ITEM_WIDTH, ITEM_HEIGHT, new Color(230, 190, 70), Item.Shape.RECTANGLE, Item.ItemType.KEY)
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
        return new Monster(
            x,
            y,
            36,
            40,
            new Color(200, 70, 70),
            6
        );
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
            Item.Shape shape = (type == Item.ItemType.KEY) ? Item.Shape.RECTANGLE : Item.Shape.OVAL;

            Color color;
            switch (type) {
                case HP_POTION -> color = new Color(80, 210, 120);
                case MP_POTION -> color = new Color(80, 140, 220);
                default -> color = new Color(230, 190, 70);
            }

            Item item = new Item(x, y, ITEM_WIDTH, ITEM_HEIGHT, color, shape, type);
            screen.addItem(item);
        }
    }
}
