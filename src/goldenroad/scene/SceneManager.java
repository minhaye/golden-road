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

        floor1Screens.add(new Screen(
            List.of(
                new Rectangle(0, 5378, 10920, 70),
                new Rectangle(220, 390, 180, 20),
                new Rectangle(560, 340, 170, 20) 
            ),
            List.of(
                new Monster(560, 1880, 36, 32, new Color(185, 80, 195), 8)
            ),
            List.of(
                new Item(280, 355, ITEM_WIDTH, ITEM_HEIGHT, new Color(80, 210, 120), Item.Shape.OVAL, Item.ItemType.HP_POTION),
                new Item(320, 355, ITEM_WIDTH, ITEM_HEIGHT, new Color(80, 140, 220), Item.Shape.OVAL, Item.ItemType.MP_POTION),
                new Item(360, 355, ITEM_WIDTH, ITEM_HEIGHT, new Color(230, 190, 70), Item.Shape.RECTANGLE, Item.ItemType.KEY)
            )
        ));

        floors.add(new Floor(floor1Screens));
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
