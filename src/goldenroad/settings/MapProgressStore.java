package goldenroad.settings;

import goldenroad.map.MapId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class MapProgressStore {
    private static final Path SAVE_FILE = Paths.get(System.getProperty("user.home"), ".golden-road-save");

    public MapId load(MapId fallbackMapId) {
        try {
            if (Files.exists(SAVE_FILE)) {
                List<String> lines = Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    return MapId.valueOf(lines.get(0).trim());
                }
            }
        } catch (Exception e) {
            // fall back to the current in-memory/default map
        }

        return fallbackMapId;
    }

    public void save(MapId mapId) {
        if (mapId == null) {
            return;
        }

        try {
            Files.write(SAVE_FILE, List.of(mapId.name()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // ignore save failures so the game keeps running
        }
    }
}
