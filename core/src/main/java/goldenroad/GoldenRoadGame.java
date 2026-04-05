package goldenroad;

import com.badlogic.gdx.Game;

public class GoldenRoadGame extends Game {
    @Override
    public void create() {
        setScreen(new PlayScreen());
    }
}
