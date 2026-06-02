package goldenroad.settings;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public final class SettingsStore {
    private static final String NODE = "goldenroad/settings";
    private static final String KEY_VOLUME = "volume";
    private static final String KEY_DIFFICULTY = "difficulty";

    private SettingsStore() {
    }

    public static GameSettings load() {
        Preferences prefs = Preferences.userRoot().node(NODE);
        int volume = prefs.getInt(KEY_VOLUME, GameSettings.DEFAULT_VOLUME);
        Difficulty difficulty = parseDifficulty(prefs.get(KEY_DIFFICULTY, Difficulty.NORMAL.name()));
        return new GameSettings(volume, difficulty);
    }

    public static void save(GameSettings settings) {
        if (settings == null) {
            return;
        }

        Preferences prefs = Preferences.userRoot().node(NODE);
        prefs.putInt(KEY_VOLUME, settings.getVolume());
        prefs.put(KEY_DIFFICULTY, settings.getDifficulty().name());

        try {
            prefs.flush();
        } catch (BackingStoreException e) {
            // Settings are best-effort; keep the game running if the OS store is unavailable.
        }
    }

    private static Difficulty parseDifficulty(String value) {
        try {
            return Difficulty.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Difficulty.NORMAL;
        }
    }
}
