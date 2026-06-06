package goldenroad.settings;

import goldenroad.entity.item.Item;
import goldenroad.entity.monster.Direction;
import goldenroad.entity.monster.MonsterState;
import goldenroad.map.MapId;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class GameSaveData {
    public static final int SAVE_VERSION = 1;

    private int saveVersion = SAVE_VERSION;
    private MapId currentMapId = MapId.MAP_0;
    private boolean minimapVisible = true;
    private PlayerSnapshot player = new PlayerSnapshot();
    private Map<Item.ItemType, Integer> inventoryCounts = new EnumMap<>(Item.ItemType.class);
    private List<MonsterSnapshot> monsters = new ArrayList<>();
    private List<ItemSnapshot> items = new ArrayList<>();

    public int getSaveVersion() {
        return saveVersion;
    }

    public void setSaveVersion(int saveVersion) {
        this.saveVersion = saveVersion;
    }

    public MapId getCurrentMapId() {
        return currentMapId;
    }

    public void setCurrentMapId(MapId currentMapId) {
        this.currentMapId = currentMapId;
    }

    public boolean isMinimapVisible() {
        return minimapVisible;
    }

    public void setMinimapVisible(boolean minimapVisible) {
        this.minimapVisible = minimapVisible;
    }

    public PlayerSnapshot getPlayer() {
        return player;
    }

    public void setPlayer(PlayerSnapshot player) {
        this.player = player == null ? new PlayerSnapshot() : player;
    }

    public Map<Item.ItemType, Integer> getInventoryCounts() {
        return inventoryCounts;
    }

    public void setInventoryCounts(Map<Item.ItemType, Integer> inventoryCounts) {
        this.inventoryCounts = inventoryCounts == null ? new EnumMap<>(Item.ItemType.class) : inventoryCounts;
    }

    public List<MonsterSnapshot> getMonsters() {
        return monsters;
    }

    public void setMonsters(List<MonsterSnapshot> monsters) {
        this.monsters = monsters == null ? new ArrayList<>() : monsters;
    }

    public List<ItemSnapshot> getItems() {
        return items;
    }

    public void setItems(List<ItemSnapshot> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }

    public static final class PlayerSnapshot {
        private float x;
        private float y;
        private int hp;
        private int maxHp;
        private int mp;
        private int maxMp;
        private double velocityX;
        private double velocityY;
        private boolean onGround;
        private double direction;
        private int dashDuration;
        private int dashCooldown;
        private boolean dashUsed;
        private int dashOnAirCount;
        private int leftCooldown;
        private int rightCooldown;
        private int invulnerabilityTimer;

        public float getX() { return x; }
        public void setX(float x) { this.x = x; }
        public float getY() { return y; }
        public void setY(float y) { this.y = y; }
        public int getHp() { return hp; }
        public void setHp(int hp) { this.hp = hp; }
        public int getMaxHp() { return maxHp; }
        public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
        public int getMp() { return mp; }
        public void setMp(int mp) { this.mp = mp; }
        public int getMaxMp() { return maxMp; }
        public void setMaxMp(int maxMp) { this.maxMp = maxMp; }
        public double getVelocityX() { return velocityX; }
        public void setVelocityX(double velocityX) { this.velocityX = velocityX; }
        public double getVelocityY() { return velocityY; }
        public void setVelocityY(double velocityY) { this.velocityY = velocityY; }
        public boolean isOnGround() { return onGround; }
        public void setOnGround(boolean onGround) { this.onGround = onGround; }
        public double getDirection() { return direction; }
        public void setDirection(double direction) { this.direction = direction; }
        public int getDashDuration() { return dashDuration; }
        public void setDashDuration(int dashDuration) { this.dashDuration = dashDuration; }
        public int getDashCooldown() { return dashCooldown; }
        public void setDashCooldown(int dashCooldown) { this.dashCooldown = dashCooldown; }
        public boolean isDashUsed() { return dashUsed; }
        public void setDashUsed(boolean dashUsed) { this.dashUsed = dashUsed; }
        public int getDashOnAirCount() { return dashOnAirCount; }
        public void setDashOnAirCount(int dashOnAirCount) { this.dashOnAirCount = dashOnAirCount; }
        public int getLeftCooldown() { return leftCooldown; }
        public void setLeftCooldown(int leftCooldown) { this.leftCooldown = leftCooldown; }
        public int getRightCooldown() { return rightCooldown; }
        public void setRightCooldown(int rightCooldown) { this.rightCooldown = rightCooldown; }
        public int getInvulnerabilityTimer() { return invulnerabilityTimer; }
        public void setInvulnerabilityTimer(int invulnerabilityTimer) { this.invulnerabilityTimer = invulnerabilityTimer; }
    }

    public static final class MonsterSnapshot {
        private String configName;
        private float x;
        private float y;
        private float spawnX;
        private float spawnY;
        private int hp;
        private boolean dead;
        private Direction direction = Direction.RIGHT;
        private MonsterState state = MonsterState.IDLE;
        private int attackCooldownRemaining;

        public String getConfigName() { return configName; }
        public void setConfigName(String configName) { this.configName = configName; }
        public float getX() { return x; }
        public void setX(float x) { this.x = x; }
        public float getY() { return y; }
        public void setY(float y) { this.y = y; }
        public float getSpawnX() { return spawnX; }
        public void setSpawnX(float spawnX) { this.spawnX = spawnX; }
        public float getSpawnY() { return spawnY; }
        public void setSpawnY(float spawnY) { this.spawnY = spawnY; }
        public int getHp() { return hp; }
        public void setHp(int hp) { this.hp = hp; }
        public boolean isDead() { return dead; }
        public void setDead(boolean dead) { this.dead = dead; }
        public Direction getDirection() { return direction; }
        public void setDirection(Direction direction) { this.direction = direction; }
        public MonsterState getState() { return state; }
        public void setState(MonsterState state) { this.state = state; }
        public int getAttackCooldownRemaining() { return attackCooldownRemaining; }
        public void setAttackCooldownRemaining(int attackCooldownRemaining) {
            this.attackCooldownRemaining = attackCooldownRemaining;
        }
    }

    public static final class ItemSnapshot {
        private Item.ItemType type = Item.ItemType.HP_POTION;
        private int x;
        private int y;

        public Item.ItemType getType() { return type; }
        public void setType(Item.ItemType type) { this.type = type; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }
}
