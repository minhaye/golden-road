package goldenroad.entity.monster;

public final class MonsterFactory {
    private MonsterFactory() {
    }

    public static Monster createMonster(MonsterConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }

        return switch (config.type) {
            case GROUND -> new GroundMonster(config);
            case AIRBORNE -> new AirborneMonster(config);
        };
    }

    public static Monster createFromConfig(MonsterConfig config, int x, int y, int leftBoundary, int rightBoundary) {
        Monster monster = createMonster(config);
        monster.setPosition(x, y);
        // Ensure the monster's spawn anchor matches initial placement so idle orbit centers correctly
        monster.spawnX = x;
        monster.spawnY = y;

        if (monster instanceof GroundMonster) {
            monster.setBehavior(new PatrolBehavior(leftBoundary, rightBoundary, 1.0));
        } else if (monster instanceof AirborneMonster) {
            monster.setBehavior(new AirborneBehavior(leftBoundary, rightBoundary, 1.35, 220f, 120f, 1.15f, 36f));
        }

        return monster;
    }
}