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
                new Rectangle(0, 730, 1920, 70),
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

        floor1Screens.add(new Screen(
            List.of(
                new Rectangle(0, 730, 1920, 70),
                new Rectangle(120, 360, 170, 20),
                new Rectangle(500, 400, 220, 20)
            ),
            List.of(
                new Monster(160, 320, 40, 40, new Color(220, 90, 60), 5),
                new Monster(610, 360, 34, 36, new Color(180, 60, 90), 4)
            ),
            List.of(
                new Item(545, 370, 20, 20, new Color(70, 180, 240), Item.Shape.RECTANGLE)
            )
        ));

        floor1Screens.add(new Screen(
            List.of(
                new Rectangle(0, 730, 1920, 70),
                new Rectangle(320, 330, 200, 20)
            ),
            List.of(
                new Monster(380, 290, 42, 42, new Color(170, 70, 160), 7)
            ),
            List.of(
                new Item(430, 440, 22, 22, new Color(240, 200, 70), Item.Shape.OVAL),
                new Item(350, 300, 16, 16, new Color(110, 220, 150), Item.Shape.RECTANGLE)
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
