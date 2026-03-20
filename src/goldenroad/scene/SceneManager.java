package goldenroad.scene;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import goldenroad.entity.AirMonster;
import goldenroad.entity.GroundMonster;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.entity.Monster.RenderShape;

public class SceneManager {
    private final List<Floor> floors = new ArrayList<>();

    private int currentFloorIndex = 0;
    private int currentScreenIndex = 0;

    public SceneManager() {
        setupPrototypeData();
    }

    private void setupPrototypeData() {
        final int worldHeight = 780;

        Screen map1 = new Screen(
            List.of(
                new Rectangle(0, 470, 1800, 70),
                new Rectangle(140, 410, 320, 34),
                new Rectangle(560, 350, 260, 30),
                new Rectangle(900, 290, 220, 26),
                new Rectangle(1180, 360, 290, 28),
                new Rectangle(1520, 270, 220, 24)
            ),
            List.of(
                new GroundMonster(620, 430, 44, 42, new Color(220, 70, 70), 8, 112, 460, 1750, 2.0, 8.8, 1, 8, new Color(255, 145, 120), RenderShape.OVAL),
                new AirMonster(1060, 200, 40, 36, new Color(135, 80, 200), 6, 128, 860, 1720, 110, 340, 3.0, 9.2, 1, 8, new Color(210, 140, 255), RenderShape.TRIANGLE)
            ),
            List.of(
                new Item(255, 375, 18, 18, new Color(80, 210, 120), Item.Shape.OVAL),
                new Item(950, 258, 16, 16, new Color(95, 220, 190), Item.Shape.RECTANGLE)
            ),
            1800,
            worldHeight
        );

        Screen map2 = new Screen(
            List.of(
                new Rectangle(0, 500, 2000, 70),
                new Rectangle(100, 420, 360, 34),
                new Rectangle(560, 360, 250, 28),
                new Rectangle(900, 300, 220, 26),
                new Rectangle(1180, 240, 190, 22),
                new Rectangle(1440, 330, 260, 26),
                new Rectangle(1730, 400, 210, 30)
            ),
            List.of(
                new GroundMonster(230, 460, 42, 40, new Color(230, 85, 65), 8, 120, 50, 1920, 2.2, 9.0, 1, 8, new Color(255, 150, 125), RenderShape.TRIANGLE),
                new GroundMonster(1510, 290, 40, 38, new Color(165, 85, 220), 8, 116, 1300, 1920, 2.0, 9.0, 1, 8, new Color(235, 175, 255), RenderShape.OVAL),
                new AirMonster(980, 150, 38, 36, new Color(95, 185, 230), 7, 148, 760, 1860, 80, 300, 2.5, 10.0, 1, 8, new Color(145, 220, 255), RenderShape.RECTANGLE)
            ),
            List.of(
                new Item(620, 326, 18, 18, new Color(120, 220, 150), Item.Shape.RECTANGLE),
                new Item(1215, 208, 16, 16, new Color(245, 205, 90), Item.Shape.OVAL),
                new Item(1780, 368, 16, 16, new Color(90, 200, 240), Item.Shape.RECTANGLE)
            ),
            2000,
            worldHeight
        );

        Screen map3 = new Screen(
            List.of(
                new Rectangle(0, 490, 2200, 70),
                new Rectangle(120, 430, 280, 30),
                new Rectangle(470, 380, 300, 30),
                new Rectangle(860, 330, 220, 26),
                new Rectangle(1150, 280, 220, 24),
                new Rectangle(1440, 330, 260, 26),
                new Rectangle(1740, 260, 180, 22),
                new Rectangle(1940, 390, 210, 28)
            ),
            List.of(
                new GroundMonster(540, 450, 44, 40, new Color(210, 75, 75), 9, 124, 160, 2100, 1.9, 9.4, 1, 8, new Color(255, 150, 135), RenderShape.RECTANGLE),
                new GroundMonster(1990, 350, 36, 36, new Color(175, 80, 230), 8, 120, 1900, 2150, 2.0, 9.2, 1, 8, new Color(240, 180, 255), RenderShape.TRIANGLE),
                new AirMonster(1260, 170, 40, 36, new Color(100, 190, 235), 7, 156, 930, 2100, 90, 320, 2.4, 10.2, 1, 8, new Color(150, 230, 255), RenderShape.OVAL)
            ),
            List.of(
                new Item(520, 348, 16, 16, new Color(105, 220, 170), Item.Shape.OVAL),
                new Item(1470, 298, 18, 18, new Color(250, 205, 95), Item.Shape.RECTANGLE),
                new Item(1975, 356, 16, 16, new Color(85, 200, 240), Item.Shape.OVAL)
            ),
            2200,
            worldHeight
        );

        floors.add(new Floor(List.of(map1)));
        floors.add(new Floor(List.of(map2)));
        floors.add(new Floor(List.of(map3)));
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
