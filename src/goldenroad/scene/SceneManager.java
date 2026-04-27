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

        floor1Screens.add(new Screen(
            List.of(
                new Rectangle(0, 5378, 10920, 70),
                new Rectangle(220, 390, 180, 20),
                new Rectangle(560, 340, 170, 20) 
            ),
            List.of(
                new Monster(640, 300, 36, 40, new Color(200, 70, 70), 6)
            ),
            List.of(
                new Item(280, 355, 18, 18, new Color(80, 210, 120), Item.Shape.OVAL)
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
}
