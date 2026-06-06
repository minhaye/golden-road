package goldenroad.settings;

import goldenroad.entity.item.Item;
import goldenroad.entity.monster.Direction;
import goldenroad.entity.monster.MonsterState;
import goldenroad.map.MapId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GameSaveStore {
    private static final Path SAVE_FILE = Paths.get(System.getProperty("user.home"), ".golden-road-game.json");

    public boolean hasSave() {
        return Files.isRegularFile(SAVE_FILE);
    }

    public void save(GameSaveData data) throws IOException {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null");
        }

        Files.writeString(SAVE_FILE, toJson(data), StandardCharsets.UTF_8);
    }

    public GameSaveData load() throws IOException {
        if (!hasSave()) {
            return null;
        }

        String json = Files.readString(SAVE_FILE, StandardCharsets.UTF_8);
        return fromJson(json);
    }

    private static String toJson(GameSaveData data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"saveVersion\": ").append(data.getSaveVersion()).append(",\n");
        sb.append("  \"currentMapId\": \"").append(escape(data.getCurrentMapId().name())).append("\",\n");
        sb.append("  \"minimapVisible\": ").append(data.isMinimapVisible()).append(",\n");

        GameSaveData.PlayerSnapshot player = data.getPlayer();
        sb.append("  \"player\": {\n");
        sb.append("    \"x\": ").append(player.getX()).append(",\n");
        sb.append("    \"y\": ").append(player.getY()).append(",\n");
        sb.append("    \"hp\": ").append(player.getHp()).append(",\n");
        sb.append("    \"maxHp\": ").append(player.getMaxHp()).append(",\n");
        sb.append("    \"mp\": ").append(player.getMp()).append(",\n");
        sb.append("    \"maxMp\": ").append(player.getMaxMp()).append(",\n");
        sb.append("    \"velocityX\": ").append(player.getVelocityX()).append(",\n");
        sb.append("    \"velocityY\": ").append(player.getVelocityY()).append(",\n");
        sb.append("    \"onGround\": ").append(player.isOnGround()).append(",\n");
        sb.append("    \"direction\": ").append(player.getDirection()).append(",\n");
        sb.append("    \"dashDuration\": ").append(player.getDashDuration()).append(",\n");
        sb.append("    \"dashCooldown\": ").append(player.getDashCooldown()).append(",\n");
        sb.append("    \"dashUsed\": ").append(player.isDashUsed()).append(",\n");
        sb.append("    \"dashOnAirCount\": ").append(player.getDashOnAirCount()).append(",\n");
        sb.append("    \"leftCooldown\": ").append(player.getLeftCooldown()).append(",\n");
        sb.append("    \"rightCooldown\": ").append(player.getRightCooldown()).append(",\n");
        sb.append("    \"invulnerabilityTimer\": ").append(player.getInvulnerabilityTimer()).append("\n");
        sb.append("  },\n");

        sb.append("  \"inventory\": {\n");
        boolean first = true;
        for (Item.ItemType type : Item.ItemType.values()) {
            if (!first) {
                sb.append(",\n");
            }
            first = false;
            int count = data.getInventoryCounts().getOrDefault(type, 0);
            sb.append("    \"").append(escape(type.name())).append("\": ").append(count);
        }
        sb.append("\n  },\n");

        sb.append("  \"monsters\": [\n");
        List<GameSaveData.MonsterSnapshot> monsters = data.getMonsters();
        for (int i = 0; i < monsters.size(); i++) {
            GameSaveData.MonsterSnapshot monster = monsters.get(i);
            sb.append("    {\n");
            sb.append("      \"configName\": \"").append(escape(monster.getConfigName())).append("\",\n");
            sb.append("      \"x\": ").append(monster.getX()).append(",\n");
            sb.append("      \"y\": ").append(monster.getY()).append(",\n");
            sb.append("      \"spawnX\": ").append(monster.getSpawnX()).append(",\n");
            sb.append("      \"spawnY\": ").append(monster.getSpawnY()).append(",\n");
            sb.append("      \"hp\": ").append(monster.getHp()).append(",\n");
            sb.append("      \"dead\": ").append(monster.isDead()).append(",\n");
            sb.append("      \"direction\": \"").append(escape(monster.getDirection().name())).append("\",\n");
            sb.append("      \"state\": \"").append(escape(monster.getState().name())).append("\",\n");
            sb.append("      \"attackCooldownRemaining\": ").append(monster.getAttackCooldownRemaining()).append("\n");
            sb.append("    }");
            if (i < monsters.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ],\n");

        sb.append("  \"items\": [\n");
        List<GameSaveData.ItemSnapshot> items = data.getItems();
        for (int i = 0; i < items.size(); i++) {
            GameSaveData.ItemSnapshot item = items.get(i);
            sb.append("    {\n");
            sb.append("      \"type\": \"").append(escape(item.getType().name())).append("\",\n");
            sb.append("      \"x\": ").append(item.getX()).append(",\n");
            sb.append("      \"y\": ").append(item.getY()).append("\n");
            sb.append("    }");
            if (i < items.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static GameSaveData fromJson(String json) {
        GameSaveData data = new GameSaveData();
        data.setSaveVersion((int) readNumber(json, "saveVersion", GameSaveData.SAVE_VERSION));
        data.setCurrentMapId(MapId.valueOf(readString(json, "currentMapId", MapId.MAP_0.name())));
        data.setMinimapVisible(readBoolean(json, "minimapVisible", true));

        GameSaveData.PlayerSnapshot player = new GameSaveData.PlayerSnapshot();
        String playerBlock = readObject(json, "player");
        player.setX((float) readNumber(playerBlock, "x", 0));
        player.setY((float) readNumber(playerBlock, "y", 0));
        player.setHp((int) readNumber(playerBlock, "hp", 100));
        player.setMaxHp((int) readNumber(playerBlock, "maxHp", 100));
        player.setMp((int) readNumber(playerBlock, "mp", 100));
        player.setMaxMp((int) readNumber(playerBlock, "maxMp", 100));
        player.setVelocityX(readNumber(playerBlock, "velocityX", 0));
        player.setVelocityY(readNumber(playerBlock, "velocityY", 0));
        player.setOnGround(readBoolean(playerBlock, "onGround", true));
        player.setDirection(readNumber(playerBlock, "direction", 1));
        player.setDashDuration((int) readNumber(playerBlock, "dashDuration", 0));
        player.setDashCooldown((int) readNumber(playerBlock, "dashCooldown", 0));
        player.setDashUsed(readBoolean(playerBlock, "dashUsed", false));
        player.setDashOnAirCount((int) readNumber(playerBlock, "dashOnAirCount", 0));
        player.setLeftCooldown((int) readNumber(playerBlock, "leftCooldown", 0));
        player.setRightCooldown((int) readNumber(playerBlock, "rightCooldown", 0));
        player.setInvulnerabilityTimer((int) readNumber(playerBlock, "invulnerabilityTimer", 0));
        data.setPlayer(player);

        Map<Item.ItemType, Integer> inventoryCounts = new EnumMap<>(Item.ItemType.class);
        String inventoryBlock = readObject(json, "inventory");
        for (Item.ItemType type : Item.ItemType.values()) {
            inventoryCounts.put(type, (int) readNumber(inventoryBlock, type.name(), 0));
        }
        data.setInventoryCounts(inventoryCounts);

        List<GameSaveData.MonsterSnapshot> monsters = new ArrayList<>();
        for (String block : readArrayObjects(json, "monsters")) {
            GameSaveData.MonsterSnapshot monster = new GameSaveData.MonsterSnapshot();
            monster.setConfigName(readString(block, "configName", ""));
            monster.setX((float) readNumber(block, "x", 0));
            monster.setY((float) readNumber(block, "y", 0));
            monster.setSpawnX((float) readNumber(block, "spawnX", monster.getX()));
            monster.setSpawnY((float) readNumber(block, "spawnY", monster.getY()));
            monster.setHp((int) readNumber(block, "hp", 0));
            monster.setDead(readBoolean(block, "dead", false));
            monster.setDirection(Direction.valueOf(readString(block, "direction", Direction.RIGHT.name())));
            monster.setState(MonsterState.valueOf(readString(block, "state", MonsterState.IDLE.name())));
            monster.setAttackCooldownRemaining((int) readNumber(block, "attackCooldownRemaining", 0));
            monsters.add(monster);
        }
        data.setMonsters(monsters);

        List<GameSaveData.ItemSnapshot> items = new ArrayList<>();
        for (String block : readArrayObjects(json, "items")) {
            GameSaveData.ItemSnapshot item = new GameSaveData.ItemSnapshot();
            item.setType(Item.ItemType.valueOf(readString(block, "type", Item.ItemType.HP_POTION.name())));
            item.setX((int) readNumber(block, "x", 0));
            item.setY((int) readNumber(block, "y", 0));
            items.add(item);
        }
        data.setItems(items);

        return data;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String readObject(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\{");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return "";
        }

        int start = matcher.end() - 1;
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return json.substring(start, i + 1);
                }
            }
        }
        return "";
    }

    private static List<String> readArrayObjects(String json, String key) {
        List<String> objects = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return objects;
        }

        int index = matcher.end();
        while (index < json.length()) {
            while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
                index++;
            }
            if (index >= json.length() || json.charAt(index) == ']') {
                break;
            }
            if (json.charAt(index) != '{') {
                break;
            }

            int start = index;
            int depth = 0;
            for (; index < json.length(); index++) {
                char c = json.charAt(index);
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        objects.add(json.substring(start, index + 1));
                        index++;
                        break;
                    }
                }
            }

            while (index < json.length() && (Character.isWhitespace(json.charAt(index)) || json.charAt(index) == ',')) {
                index++;
            }
        }

        return objects;
    }

    private static String readString(String json, String key, String fallback) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return fallback;
        }
        return unescape(matcher.group(1));
    }

    private static double readNumber(String json, String key, double fallback) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(-?[0-9]+(?:\\.[0-9]+)?)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return fallback;
        }
        return Double.parseDouble(matcher.group(1));
    }

    private static boolean readBoolean(String json, String key, boolean fallback) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(json);
        if (!matcher.find()) {
            return fallback;
        }
        return Boolean.parseBoolean(matcher.group(1));
    }

    private static String unescape(String value) {
        return value.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
