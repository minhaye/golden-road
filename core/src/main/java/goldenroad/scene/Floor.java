package goldenroad.scene;

import java.util.List;

public class Floor {
    private final List<ScreenData> screens;

    public Floor(List<ScreenData> screens) {
        this.screens = List.copyOf(screens);
    }

    public ScreenData getScreen(int index) {
        return screens.get(index);
    }
}
