package goldenroad.entity.player;
    
import goldenroad.entity.Entity;
import goldenroad.input.KeyHandler;
import goldenroad.settings.GameSaveData;
import java.awt.*;
import java.awt.geom.Point2D;

public class Player extends Entity {

    private enum PlayerState {
        IDLE, RUN, DASH, JUMP, FALL
    }

    private PlayerState state = PlayerState.IDLE;

    private final PlayerRenderer renderer;

    private static final int COYOTE_MAX = 12;

    // Movement delegated to PlayerMovement component
    private final PlayerMovement movement = new PlayerMovement();

    // ====== RESOURCES (delegated) ======
    private final PlayerResources resources = new PlayerResources();
    private final PlayerCombat combat = new PlayerCombat(resources);
    private final PlayerAttack attack = new PlayerAttack(this);

    // ====== SIZE ======
    private static final double WIDTH = 20;
    private static final double HEIGHT = 50;

    @Override
    public float getX() { return x; }

    @Override
    public float getY() { return y; }

    public double getVelocityX() { return movement.getVelocityX(); }
    public double getVelocityY() { return movement.getVelocityY(); }

    public void setVelocityY(double v) { this.movement.setVelocityY(v); }

    public void setX(double x) { this.x = (float)x; }

    public void setY(double y) { this.y = (float)y; }

    @Override
    public double getWidth() { return WIDTH; }

    @Override
    public double getHeight() { return HEIGHT; }

    // ====== UPDATE ======
    public void update(KeyHandler input) {
        double moveX = movement.update(input, resources);

        // ===== STATE =====
        updateState(moveX);
        // ===== ANIMATION =====
        renderer.updateAnimation();
        // update combat timers (invulnerability, etc.)
        combat.update();
        // update attack cooldowns
        attack.tick();
    }

    public void setOnGround(boolean val) {
        movement.setOnGround(val);
    }

    public boolean isDroppingDown() {
        return movement.isDroppingDown();
    }

    public boolean isIdle() {
        return state == PlayerState.IDLE;
    }

    public double getDirection() {
        return movement.getDirection();
    }
    public int getHp() { return resources.getHp(); }
    public int getMaxHp() { return resources.getMaxHp(); }
    public int getMp() { return resources.getMp(); }
    public int getMaxMp() { return resources.getMaxMp(); }

    public int getDashCooldown() { return movement.getDashCooldown(); }
    public int getDashCooldownMax() { return movement.getDashCooldownMax(); }
    public float getDashCooldownRatio() { return movement.getDashCooldownRatio(); }

    public void updateResources() { resources.updateResources(); }
    public boolean spendMp(int cost) { return resources.spendMp(cost); }
    public void heal(int amount) { resources.heal(amount); }
    public void restoreMp(int amount) { resources.restoreMp(amount); }
    public void takeDamage(int damage) { combat.applyDamage(damage); }

    public PlayerAttack getAttack() { return attack; }

    public Player(float x, float y) {
        super(x, y);
        this.renderer = new PlayerRenderer(this);
    }

    // ====== STATE ======
    private void updateState(double moveX) {
        switch (state) {
        case DASH:
        if (movement.getDashDuration() <= 0) {
            // chỉ chuyển khi dash kết thúc
            state = (moveX != 0) ? PlayerState.RUN : PlayerState.IDLE;
        }
        break;

        default:
            if (movement.getDashDuration() > 0) {
                state = PlayerState.DASH;
            } else if (!movement.isOnGround()) {
                if (movement.getVelocityY() < 0) state =  PlayerState.JUMP ;
                if (movement.getVelocityY() > 0 && movement.getCoyoteTime() < COYOTE_MAX/2) state =  PlayerState.FALL;
            } else if (moveX != 0) {
                state = PlayerState.RUN;
            } else {
            state = PlayerState.IDLE;
            }
        }
    }

    // ====== ANIMATION ======
    @Override
    public void render(Graphics2D g) {
        renderer.render(g);
    }

    public void render(Graphics2D g, boolean aiming, double aimX, double aimY) {
        renderer.render(g, aiming, aimX, aimY);
    }

    public void draw(Graphics2D g) {
        render(g);
    }

    public void draw(Graphics2D g, boolean aiming, double aimX, double aimY) {
        render(g, aiming, aimX, aimY);
    }

    public Point2D.Double getGunCenter(double aimX, double aimY) {
        return renderer.getGunCenter(aimX, aimY);
    }

    int getAnimationStateKey() {
        return state.ordinal();
    }

    int getAnimationFrameCount() {
        return switch (state) {
            case IDLE -> 12;
            case RUN -> 12;
            case DASH -> 4;
            case JUMP -> 4;
            case FALL -> 4;
        };
    }

    int getAnimationRow() {
        return switch (state) {
            case IDLE -> 0;
            case RUN -> 1;
            case DASH -> 2;
            case JUMP -> 3;
            case FALL -> 4;
        };
    }

    public PlayerState getState() { return state; }

    public GameSaveData.PlayerSnapshot captureSnapshot() {
        GameSaveData.PlayerSnapshot snapshot = new GameSaveData.PlayerSnapshot();
        snapshot.setX(getX());
        snapshot.setY(getY());
        snapshot.setHp(getHp());
        snapshot.setMaxHp(getMaxHp());
        snapshot.setMp(getMp());
        snapshot.setMaxMp(getMaxMp());
        movement.captureState(snapshot);
        snapshot.setLeftCooldown(attack.getLeftCooldown());
        snapshot.setRightCooldown(attack.getRightCooldown());
        snapshot.setInvulnerabilityTimer(combat.getInvulnerabilityTimer());
        return snapshot;
    }

    public void applySnapshot(GameSaveData.PlayerSnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        setX(snapshot.getX());
        setY(snapshot.getY());
        resources.setMaxHp(snapshot.getMaxHp());
        resources.setMaxMp(snapshot.getMaxMp());
        resources.setHp(snapshot.getHp());
        resources.setMp(snapshot.getMp());
        movement.applyState(snapshot);
        attack.applyCooldowns(snapshot.getLeftCooldown(), snapshot.getRightCooldown());
        combat.setInvulnerabilityTimer(snapshot.getInvulnerabilityTimer());
    }
}
