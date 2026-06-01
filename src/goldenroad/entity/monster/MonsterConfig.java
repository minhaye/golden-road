package goldenroad.entity.monster;

public class MonsterConfig {
    public final MonsterType type;
    public final String name;
    public final int hp;
    public final int damage;
    public final float attackSpeed;
    public final float moveSpeed;
    public final int width;
    public final int height;
    public final float moveRange;
    public final float detectRange;
    public final float attackRange;
    public final float frameDuration;
    public final String assetBasePath;

    public MonsterConfig(
            MonsterType type,
            String name,
            int hp,
            int damage,
            float attackSpeed,
            float moveSpeed,
            int width,
            int height,
            float moveRange,
            float detectRange,
            float attackRange,
            float frameDuration,
            String assetBasePath
    ) {
        this.type = type;
        this.name = name;
        this.hp = hp;
        this.damage = damage;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
        this.width = width;
        this.height = height;
        this.moveRange = moveRange;
        this.detectRange = detectRange;
        this.attackRange = attackRange;
        this.frameDuration = frameDuration;
        this.assetBasePath = assetBasePath;
    }
}