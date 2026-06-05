package goldenroad.map;

import goldenroad.entity.monster.MonsterType;
import java.util.Objects;

public final class MonsterSpawnPoint {
    private final int x;
    private final int y;
    private final MonsterType type;
    private final int configIndex;

    public MonsterSpawnPoint(int x, int y, MonsterType type, int configIndex) {
        this.x = x;
        this.y = y;
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.configIndex = configIndex;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public MonsterType getType() {
        return type;
    }

    public int getConfigIndex() {
        return configIndex;
    }
}
