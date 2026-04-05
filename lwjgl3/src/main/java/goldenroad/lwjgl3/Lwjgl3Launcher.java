package goldenroad.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import goldenroad.GoldenRoadGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new GoldenRoadGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Golden Road (LibGDX)");
        configuration.setWindowedMode(1280, 720);
        configuration.setResizable(true);
        configuration.useVsync(true);
        configuration.setForegroundFPS(60);
        return configuration;
    }
}
