package dev.kitteh.factions.data.json;

import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Morton;
import dev.kitteh.factions.util.WorldTracker;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;

@NullMarked
public final class JSONBoard extends MemoryBoard {
    private final Path boardPath;

    public JSONBoard() {
        this.boardPath = AbstractFactionsPlugin.instance().getDataFolder().toPath().resolve("data/board.json");
    }

    private Map<String, Map<String, String>> dumpAsSaveFormat() {
        Map<String, Map<String, String>> worldCoordIds = new HashMap<>();

        this.worldTrackers.forEach((world, tracker) -> {
            Map<String, String> worldMap = new TreeMap<>();
            worldCoordIds.put(world, worldMap);
            tracker.chunkIdMapForSave().forEach((chunk, faction) -> {
                int x = Morton.getX(chunk);
                int z = Morton.getZ(chunk);
                worldMap.put(x + "," + z, String.valueOf(faction));
            });
        });

        return worldCoordIds;
    }

    private void loadFromSaveFormat(Map<String, Map<String, String>> worldCoordIds) {
        this.worldTrackers.clear();

        String worldName;
        String[] coords;
        int x, z;
        int factionId;

        for (Entry<String, Map<String, String>> entry : worldCoordIds.entrySet()) {
            worldName = entry.getKey();
            WorldTracker tracker = this.getAndCreate(worldName);
            for (Entry<String, String> entry2 : entry.getValue().entrySet()) {
                coords = entry2.getKey().trim().split("[,\\s]+");
                x = Integer.parseInt(coords[0]);
                z = Integer.parseInt(coords[1]);
                try {
                    factionId = Integer.parseInt(entry2.getValue().trim());
                } catch (NumberFormatException ex) {
                    AbstractFactionsPlugin.instance().getLogger().warning("Found invalid faction ID '" + entry2.getValue() + "' in " + worldName + " at " + entry2.getKey());
                    continue; // NOPE
                }

                tracker.addClaimOnLoad(factionId, x, z);
            }
        }
    }

    @Override
    public void forceSave(boolean sync) {
        Map<String, Map<String, String>> map = dumpAsSaveFormat();
        JsonSaver.write(boardPath, () -> AbstractFactionsPlugin.instance().gson().toJson(map), sync);
    }

    @Override
    public int load() {
        if (!Files.exists(boardPath)) {
            AbstractFactionsPlugin.instance().getLogger().info("No board to load from disk. Creating new file.");
            forceSave(true);
            return 0;
        }

        try {
            Type type = new TypeToken<Map<String, Map<String, String>>>() {
            }.getType();
            Map<String, Map<String, String>> worldCoordIds = AbstractFactionsPlugin.instance().gson().fromJson(Files.newBufferedReader(boardPath), type);
            loadFromSaveFormat(worldCoordIds);
        } catch (Exception e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to load the board from disk.", e);
            return 0;
        }

        return this.getTotalCount();
    }
}
