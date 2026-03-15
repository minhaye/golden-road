package goldenroad.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Floor {
    private final List<Screen> screens;

    public Floor(List<Screen> screens) {
        this.screens = new ArrayList<>(screens);
    }

    public Screen getScreen(int index) {
        return screens.get(index);
    }

    public boolean hasScreen(int index) {
        return index >= 0 && index < screens.size();
    }

    public int getScreenCount() {
        return screens.size();
    }

    public List<Screen> getScreens() {
        return Collections.unmodifiableList(screens);
    }
}
