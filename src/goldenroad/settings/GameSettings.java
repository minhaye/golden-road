package goldenroad.settings;

public final class GameSettings {
    public static final int DEFAULT_VOLUME = 80;

    private int volume;
    private Difficulty difficulty;

    public GameSettings() {
        this(DEFAULT_VOLUME, Difficulty.NORMAL);
    }

    public GameSettings(int volume, Difficulty difficulty) {
        setVolume(volume);
        setDifficulty(difficulty);
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
    }
}
