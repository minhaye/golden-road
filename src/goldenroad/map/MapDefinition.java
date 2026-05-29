package goldenroad.map;

public final class MapDefinition {
    private final MapId id;
    private final String backgroundPath;
    private final String hiddenPath;
    private final String collisionPath;
    private final int worldWidth;
    private final int worldHeight;
    private final int spawnX;
    private final int spawnY;

    public MapDefinition(
        MapId id,
        String backgroundPath,
        String hiddenPath,
        String collisionPath,
        int worldWidth,
        int worldHeight,
        int spawnX,
        int spawnY
    ) {
        this.id = id;
        this.backgroundPath = backgroundPath;
        this.hiddenPath = hiddenPath;
        this.collisionPath = collisionPath;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
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
}