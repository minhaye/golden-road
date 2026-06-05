package goldenroad.map;

import java.util.List;

public final class MapDefinition {
    private final MapId id;
    private final String backgroundPath;
    private final String hiddenPath;
    private final String collisionPath;
    private final String musicPath;
    private final int worldWidth;
    private final int worldHeight;
    private final int spawnX;
    private final int spawnY;
    private final List<MonsterSpawnPoint> monsterSpawnPoints;

    public MapDefinition(
        MapId id,
        String backgroundPath,
        String hiddenPath,
        String collisionPath,
        String musicPath,
        int worldWidth,
        int worldHeight,
        int spawnX,
        int spawnY,
        List<MonsterSpawnPoint> monsterSpawnPoints
    ) {
        this.id = id;
        this.backgroundPath = backgroundPath;
        this.hiddenPath = hiddenPath;
        this.collisionPath = collisionPath;
        this.musicPath = musicPath;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.monsterSpawnPoints = monsterSpawnPoints;
    }

    public MapId getId() {
        return id;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public String getHiddenPath() {
        return hiddenPath;
    }

    public String getCollisionPath() {
        return collisionPath;
    }

    public String getMusicPath() {
        return musicPath;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public int getSpawnX() {
        return spawnX;
    }

    public int getSpawnY() {
        return spawnY;
    }

    public List<MonsterSpawnPoint> getMonsterSpawnPoints() {
        return monsterSpawnPoints;
    }
}
