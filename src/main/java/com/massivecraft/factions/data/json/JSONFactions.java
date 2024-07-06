package com.massivecraft.factions.data.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.data.MemoryFaction;
import com.massivecraft.factions.data.MemoryFactions;
import com.massivecraft.factions.util.DiscUtil;
import com.massivecraft.factions.util.UUIDFetcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class JSONFactions extends MemoryFactions {
    public Gson getGson() {
        return FactionsPlugin.getInstance().getGson();
    }

    private final File file;

    public File getFile() {
        return file;
    }

    // -------------------------------------------- //
    // CONSTRUCTORS
    // -------------------------------------------- //

    public JSONFactions() {
        if (FactionsPlugin.getInstance().getServerUUID() == null) {
            FactionsPlugin.getInstance().grumpException(new RuntimeException());
        }
        this.file = new File(FactionsPlugin.getInstance().getDataFolder(), "data/factions.json");
        this.nextId = 1;
    }

    public void forceSave() {
        forceSave(true);
    }

    public void forceSave(boolean sync) {
        final Map<String, JSONFaction> entitiesThatShouldBeSaved = new HashMap<>();
        for (Faction entity : this.factions.values()) {
            entitiesThatShouldBeSaved.put(entity.getId(), (JSONFaction) entity);
        }

        JSONFaction f = new JSONFaction("```storage``");
        f.setMaxVaults(this.nextId);
        f.setDescription("Storage-only faction, not present in game. Do not touch.");
        f.setFoundedDate(0);
        f.getPermissions().clear();
        f.setTag("MissingNo.");
        entitiesThatShouldBeSaved.put("```storage``", f);
        saveCore(file, entitiesThatShouldBeSaved, sync);
    }

    private boolean saveCore(File target, Map<String, JSONFaction> entities, boolean sync) {
        return DiscUtil.writeCatch(target, FactionsPlugin.getInstance().getGson().toJson(entities), sync);
    }

    public int load() {
        Map<String, JSONFaction> factions = this.loadCore();
        if (factions != null) {
            Faction storage = factions.remove("```storage``");
            if (storage != null) {
                this.nextId = Math.max(this.nextId, storage.getMaxVaults());
            }
            this.factions.putAll(factions);
        }

        super.load();
        return this.factions.size();
    }

    private Map<String, JSONFaction> loadCore() {
        if (!this.file.exists()) {
            return new HashMap<>();
        }

        String content = DiscUtil.readCatch(this.file);
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        Map<String, JSONFaction> data = FactionsPlugin.getInstance().getGson().fromJson(content, new TypeToken<Map<String, JSONFaction>>() {
        }.getType());

        this.nextId = 1;
        // Do we have any names that need updating in claims or invites?

        int needsUpdate = 0;
        for (Entry<String, JSONFaction> entry : data.entrySet()) {
            String id = entry.getKey();
            MemoryFaction f = entry.getValue();
            f.checkPerms();
            f.setId(id);
            this.updateNextIdForId(id);
            needsUpdate += whichKeysNeedMigration(f.getInvites()).size();
            for (Set<String> keys : f.getClaimOwnership().values()) {
                needsUpdate += whichKeysNeedMigration(keys).size();
            }
        }

        if (needsUpdate > 0) {
            // We've got some converting to do!
            FactionsPlugin.getInstance().log(Level.INFO, "Factions is now updating factions.json");

            // First we'll make a backup, because god forbid anybody heed a
            // warning
            File file = new File(this.file.getParentFile(), "factions.json.old");
            try {
                file.createNewFile();
            } catch (IOException e) {
                FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to create file factions.json.old", e);
            }
            saveCore(file, data, true);
            FactionsPlugin.getInstance().log(Level.INFO, "Backed up your old data at " + file.getAbsolutePath());

            FactionsPlugin.getInstance().log(Level.INFO, "Please wait while Factions converts " + needsUpdate + " old player names to UUID. This may take a while.");

            // Update claim ownership

            for (String string : data.keySet()) {
                Faction f = data.get(string);
                Map<FLocation, Set<String>> claims = f.getClaimOwnership();
                for (FLocation key : claims.keySet()) {
                    Set<String> set = claims.get(key);

                    Set<String> list = whichKeysNeedMigration(set);

                    if (!list.isEmpty()) {
                        UUIDFetcher fetcher = new UUIDFetcher(new ArrayList<>(list));
                        try {
                            Map<String, UUID> response = fetcher.call();
                            for (String value : response.keySet()) {
                                // Let's replace their old named entry with a
                                // UUID key
                                String id = response.get(value).toString();
                                set.remove(value.toLowerCase()); // Out with the
                                // old...
                                set.add(id); // And in with the new
                            }
                        } catch (Exception e) {
                            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Encountered exception looking up UUIDs", e);
                        }
                        claims.put(key, set); // Update
                    }
                }
            }

            // Update invites

            for (String string : data.keySet()) {
                Faction f = data.get(string);
                Set<String> invites = f.getInvites();
                Set<String> list = whichKeysNeedMigration(invites);

                if (!list.isEmpty()) {
                    UUIDFetcher fetcher = new UUIDFetcher(new ArrayList<>(list));
                    try {
                        Map<String, UUID> response = fetcher.call();
                        for (String value : response.keySet()) {
                            // Let's replace their old named entry with a UUID
                            // key
                            String id = response.get(value).toString();
                            invites.remove(value.toLowerCase()); // Out with the
                            // old...
                            invites.add(id); // And in with the new
                        }
                    } catch (Exception e) {
                        FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Encountered exception looking up UUIDs", e);
                    }
                }
            }

            saveCore(this.file, data, true); // Update the flatfile
            FactionsPlugin.getInstance().log(Level.INFO, "Done converting factions.json to UUID.");
        }
        return data;
    }

    private Set<String> whichKeysNeedMigration(Set<String> keys) {
        HashSet<String> list = new HashSet<>();
        for (String value : keys) {
            if (!value.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
                // Not a valid UUID..
                if (value.matches("[a-zA-Z0-9_]{2,16}")) {
                    // Valid playername, we'll mark this as one for conversion
                    // to UUID
                    list.add(value);
                }
            }
        }
        return list;
    }

    // -------------------------------------------- //
    // ID MANAGEMENT
    // -------------------------------------------- //

    public String getNextId() {
        while (!isIdFree(this.nextId)) {
            this.nextId += 1;
        }
        return Integer.toString(this.nextId++);
    }

    public boolean isIdFree(String id) {
        return !this.factions.containsKey(id);
    }

    public boolean isIdFree(int id) {
        return this.isIdFree(Integer.toString(id));
    }

    protected synchronized void updateNextIdForId(int id) {
        if (this.nextId < id) {
            this.nextId = id + 1;
        }
    }

    protected void updateNextIdForId(String id) {
        try {
            int idAsInt = Integer.parseInt(id);
            this.updateNextIdForId(idAsInt);
        } catch (Exception ignored) {
        }
    }

    @Override
    public Faction generateFactionObject() {
        String id = getNextId();
        Faction faction = new JSONFaction(id);
        updateNextIdForId(id);
        return faction;
    }

    @Override
    public Faction generateFactionObject(String id) {
        return new JSONFaction(id);
    }

    @Override
    public void convertFrom(MemoryFactions old) {
        old.factions.forEach((tag, faction) -> this.factions.put(tag, new JSONFaction((MemoryFaction) faction)));
        this.nextId = old.nextId;
        forceSave();
        Factions.instance = this;
    }
}
