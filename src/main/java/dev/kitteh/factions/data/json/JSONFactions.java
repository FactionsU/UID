package dev.kitteh.factions.data.json;

import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.MemoryFaction;
import dev.kitteh.factions.data.MemoryFactions;
import dev.kitteh.factions.util.DiscUtil;
import dev.kitteh.factions.util.OldJSONFactionDeserializer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@NullMarked
public final class JSONFactions extends MemoryFactions {
    private record NextId(int next, String BIG_WARNING) {
        NextId(int next) {
            this(next, "DO NOT DELETE OR EDIT THIS FILE UNLESS DELETING ALL FACTIONS AS WELL.");
        }
    }

    public Gson getGson() {
        return FactionsPlugin.getInstance().getGson();
    }

    private final File file;
    private final File nextIdFile;

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
        this.nextIdFile = new File(FactionsPlugin.getInstance().getDataFolder(), "data/nextFactionId.json");
        this.nextId = 1;
    }

    @Override
    public void forceSave(boolean sync) {
        final List<Faction> entitiesThatShouldBeSaved = new ArrayList<>(this.factions.values());
        DiscUtil.writeCatch(file, FactionsPlugin.getInstance().getGson().toJson(entitiesThatShouldBeSaved), sync);
        DiscUtil.writeCatch(this.nextIdFile, FactionsPlugin.getInstance().getGson().toJson(new NextId(this.nextId)) ,sync);
    }

    @Override
    public int load() {
        List<JSONFaction> factions = this.loadCore();
        if (factions != null) {
            factions.forEach(f -> {
                this.factions.put(f.getIntId(), f);
                this.updateNextIdForId(f.getIntId());
            });
        }

        super.load();
        return this.factions.size();
    }

    private @Nullable List<JSONFaction> loadCore() {
        if (!this.file.exists()) {
            return null;
        }

        String content = DiscUtil.readCatch(this.file);
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        this.nextId = 1;

        if (content.startsWith("{")) {
            Gson gson = FactionsPlugin.getInstance().getGsonBuilder(false)
                    .registerTypeAdapter(JSONFaction.class, new OldJSONFactionDeserializer())
                    .create();
            Map<String, JSONFaction> data = gson.fromJson(content, new TypeToken<Map<String, JSONFaction>>() {
            }.getType());
            Faction storage = data.remove("```storage``");
            if (storage != null) {
                this.nextId = Math.max(this.nextId, storage.getMaxVaults());
            }
            for (Entry<String, JSONFaction> entry : data.entrySet()) {
                String id = entry.getKey();
                MemoryFaction f = entry.getValue();
                f.checkPerms();
                f.setId(id);
                this.updateNextIdForId(id);
            }
            return new ArrayList<>(data.values());
        } else {
            String nextIdData = DiscUtil.readCatch(this.nextIdFile);
            NextId next = FactionsPlugin.getInstance().getGson().fromJson(nextIdData, NextId.class);
            if (next!=null) {
                this.nextId = next.next();
            }
            return FactionsPlugin.getInstance().getGson().fromJson(content, new TypeToken<List<JSONFaction>>() {
            }.getType());
        }
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
        return !this.factions.containsKey(Integer.parseInt(id));
    }

    public boolean isIdFree(int id) {
        return this.isIdFree(Integer.toString(id));
    }

    private synchronized void updateNextIdForId(int id) {
        if (this.nextId < id) {
            this.nextId = id + 1;
        }
    }

    private void updateNextIdForId(String id) {
        try {
            int idAsInt = Integer.parseInt(id);
            this.updateNextIdForId(idAsInt);
        } catch (Exception ignored) {
        }
    }

    @Override
    public MemoryFaction generateFactionObject() {
        String id = getNextId();
        MemoryFaction faction = new JSONFaction(id);
        updateNextIdForId(id);
        return faction;
    }

    @Override
    public MemoryFaction generateFactionObject(int id) {
        return new JSONFaction(id);
    }
}
