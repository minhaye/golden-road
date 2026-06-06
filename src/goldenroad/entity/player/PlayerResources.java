package goldenroad.entity.player;

public class PlayerResources {
    private int hp = 100;
    private int maxHp = 100;
    private int mp = 100;
    private int maxMp = 100;
    private double mpRegenAccumulator = 0;
    private static final double MP_REGEN_PER_FRAME = 5.0 / 60.0;

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getMp() { return mp; }
    public int getMaxMp() { return maxMp; }

    public void updateResources() {
        if (mp >= maxMp) {
            mpRegenAccumulator = 0;
            return;
        }

        mpRegenAccumulator += MP_REGEN_PER_FRAME;
        while (mpRegenAccumulator >= 1 && mp < maxMp) {
            mp++;
            mpRegenAccumulator -= 1;
        }
    }

    public boolean spendMp(int cost) {
        if (cost <= 0) return true;
        if (mp < cost) return false;
        mp -= cost;
        return true;
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        hp = Math.min(maxHp, hp + amount);
    }

    public void restoreMp(int amount) {
        if (amount <= 0) return;
        mp = Math.min(maxMp, mp + amount);
    }

    public void takeDamage(int damage) {
        if (damage <= 0) return;
        hp = Math.max(0, hp - damage);
    }

    public void setHp(int hp) {
        this.hp = Math.max(0, Math.min(maxHp, hp));
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = Math.max(1, maxHp);
        this.hp = Math.min(this.hp, this.maxHp);
    }

    public void setMp(int mp) {
        this.mp = Math.max(0, Math.min(maxMp, mp));
    }

    public void setMaxMp(int maxMp) {
        this.maxMp = Math.max(1, maxMp);
        this.mp = Math.min(this.mp, this.maxMp);
    }
}
