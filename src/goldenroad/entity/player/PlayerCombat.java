package goldenroad.entity.player;

public class PlayerCombat {
    private final PlayerResources resources;

    private int invulnerabilityTimer = 0;
    private final int INVULNERABILITY_DURATION = 60; // frames

    public PlayerCombat(PlayerResources resources) {
        this.resources = resources;
    }

    public void update() {
        if (invulnerabilityTimer > 0) invulnerabilityTimer--;
    }

    public boolean isInvulnerable() {
        return invulnerabilityTimer > 0;
    }

    // Apply damage if not invulnerable. Returns true if damage applied.
    public boolean applyDamage(int damage) {
        if (damage <= 0) return false;
        if (isInvulnerable()) return false;

        resources.takeDamage(damage);
        invulnerabilityTimer = INVULNERABILITY_DURATION;
        return true;
    }

    public int getInvulnerabilityTimer() {
        return invulnerabilityTimer;
    }

    public void setInvulnerabilityTimer(int invulnerabilityTimer) {
        this.invulnerabilityTimer = Math.max(0, invulnerabilityTimer);
    }
}
