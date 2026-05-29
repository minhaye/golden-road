package goldenroad.scene;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import goldenroad.entity.Item;
import goldenroad.entity.Monster;

public class SceneManager {
    private final List<Floor> floors = new ArrayList<>();

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
                new Item(280, 355, 18, 18, new Color(80, 210, 120), Item.Shape.OVAL)
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
}
