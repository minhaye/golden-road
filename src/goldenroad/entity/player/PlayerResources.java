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
}
