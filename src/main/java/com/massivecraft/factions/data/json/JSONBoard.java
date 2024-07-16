package com.massivecraft.factions.data.json;

import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.data.MemoryBoard;
import com.massivecraft.factions.util.DiscUtil;

import java.io.File;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;


public class JSONBoard extends MemoryBoard {
    private static final transient File file = new File(FactionsPlugin.getInstance().getDataFolder(), "data/board.json");

    // -------------------------------------------- //
    // Persistance
    // -------------------------------------------- //

    public Map<String, Map<String, String>> dumpAsSaveFormat() {
        Map<String, Map<String, String>> worldCoordIds = new HashMap<>();

        this.worldTrackers.forEach((world, tracker) -> {
            Map<String, String> worldMap = new TreeMap<>();
            worldCoordIds.put(world, worldMap);
            tracker.getChunkToFactionForSave().forEach((chunk, faction) -> {
                int x = Morton.getX(chunk);
                int z = Morton.getZ(chunk);
                worldMap.put(x + "," + z, String.valueOf(faction));
            });
        });

        return worldCoordIds;
    }

    public void loadFromSaveFormat(Map<String, Map<String, String>> worldCoordIds) {
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
                    FactionsPlugin.getInstance().getLogger().warning("Found invalid faction ID '" + entry2.getValue() + "' in " + worldName + " at " + entry2.getKey());
                    continue; // NOPE
                }

                tracker.addClaimOnLoad(factionId, x, z);
            }
        }
    }

    public void forceSave() {
        forceSave(true);
    }

    public void forceSave(boolean sync) {
        Map<String, Map<String, String>> map = dumpAsSaveFormat();
        DiscUtil.write(file, () -> FactionsPlugin.getInstance().getGson().toJson(map), sync);
    }

    public int load() {
        if (!file.exists()) {
            FactionsPlugin.getInstance().getLogger().info("No board to load from disk. Creating new file.");
            forceSave();
            return 0;
        }

        try {
            Type type = new TypeToken<Map<String, Map<String, String>>>() {
            }.getType();
            Map<String, Map<String, String>> worldCoordIds = FactionsPlugin.getInstance().getGson().fromJson(DiscUtil.read(file), type);
            loadFromSaveFormat(worldCoordIds);
        } catch (Exception e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to load the board from disk.", e);
            return 0;
        }

        return this.getTotalCount();
    }

    @Override
    public void convertFrom(MemoryBoard old) {
        throw new UnsupportedOperationException();
    }
}
