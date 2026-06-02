package goldenroad.settings;

public enum Difficulty {
    EASY("Easy", 0.85f, 0.85f, 1.25f),
    NORMAL("Normal", 1.0f, 1.0f, 1.0f),
    HARD("Hard", 1.25f, 1.4f, 0.6f);

    private final String displayName;
    private final float monsterMoveMultiplier;
    private final float monsterAttackSpeedMultiplier;
    private final float hpMpItemMultiplier;

    Difficulty(
        String displayName,
        float monsterMoveMultiplier,
        float monsterAttackSpeedMultiplier,
        float hpMpItemMultiplier
    ) {
        this.displayName = displayName;
        this.monsterMoveMultiplier = monsterMoveMultiplier;
        this.monsterAttackSpeedMultiplier = monsterAttackSpeedMultiplier;
        this.hpMpItemMultiplier = hpMpItemMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getMonsterMoveMultiplier() {
        return monsterMoveMultiplier;
    }

    public float getMonsterAttackSpeedMultiplier() {
        return monsterAttackSpeedMultiplier;
    }

    public float getHpMpItemMultiplier() {
        return hpMpItemMultiplier;
    }
}
