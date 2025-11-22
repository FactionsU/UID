package dev.kitteh.factions.data.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.data.MemoryFaction;
import dev.kitteh.factions.data.MemoryFactions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.adapter.OldJSONFactionDeserializer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

@NullMarked
public final class JSONFactions extends MemoryFactions {
    private record NextId(int next, String BIG_WARNING) {
        NextId(int next) {
            this(next, "DO NOT DELETE OR EDIT THIS FILE UNLESS DELETING ALL FACTIONS AS WELL.");
        }
    }

    private final Path factionsPath;
    private final Path nextIdPath;

    public JSONFactions() {
        if (AbstractFactionsPlugin.instance().getServerUUID() == null) {
            AbstractFactionsPlugin.instance().grumpException(new RuntimeException());
        }
        this.factionsPath = AbstractFactionsPlugin.instance().getDataFolder().toPath().resolve("data/factions.json");
        this.nextIdPath = AbstractFactionsPlugin.instance().getDataFolder().toPath().resolve("data/nextFactionId.json");
        this.nextId = 1;
    }

    @Override
    public void forceSave(boolean sync) {
        final List<Faction> entitiesThatShouldBeSaved = new ArrayList<>(this.factions.values());
        // Serialize sync, write (a)sync
        String json = AbstractFactionsPlugin.instance().gson().toJson(entitiesThatShouldBeSaved);
        JsonSaver.write(factionsPath, () -> json, sync);
        String jsonId = AbstractFactionsPlugin.instance().gson().toJson(new NextId(this.nextId));
        JsonSaver.write(this.nextIdPath, () -> jsonId, sync);
    }

    @Override
    public int load() {
        List<JSONFaction> factions = this.loadCore();
        if (factions != null) {
            List<JSONFaction> reRun = new ArrayList<>();
            factions.forEach(f -> {
                if (f.id() == Integer.MIN_VALUE) {
                    reRun.add(f);
                } else {
                    this.factions.put(f.id(), f);
                    this.updateNextIdForId(f.id());
                }
            });
            reRun.forEach(f -> {
                f.setId(this.nextId++);
                this.factions.put(f.id(), f);
            });
        }

        super.load();
        return this.factions.size();
    }

    private @Nullable List<JSONFaction> loadCore() {
        if (!Files.exists(this.factionsPath)) {
            return null;
        }

        String content;
        try {
            content = Files.readString(this.factionsPath);
        } catch (IOException e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to read file " + this.factionsPath, e);
            return null;
        }
        if (content.trim().isEmpty()) {
            return null;
        }

        this.nextId = 1;

        if (content.startsWith("{")) {
            Gson gson = AbstractFactionsPlugin.instance().getGsonBuilder(false)
                    .registerTypeAdapter(JSONFaction.class, new OldJSONFactionDeserializer())
                    .create();
            Map<String, JSONFaction> data = gson.fromJson(content, new TypeToken<Map<String, JSONFaction>>() {
            }.getType());
            Faction storage = data.remove("```storage``");
            if (storage != null) {
                this.nextId = Math.max(this.nextId, storage.maxVaults());
            }
            for (Entry<String, JSONFaction> entry : data.entrySet()) {
                String id = entry.getKey();
                MemoryFaction f = entry.getValue();
                try {
                    f.setId(Integer.parseInt(id));
                    this.updateNextIdForId(Integer.parseInt(id));
                } catch (NumberFormatException e) {
                    f.setId(Integer.MIN_VALUE);
                }
            }
            return new ArrayList<>(data.values());
        } else {
            String nextIdData = "";
            try {
                nextIdData = Files.readString(this.nextIdPath);
            } catch (IOException ignored) {
            }
            NextId next = AbstractFactionsPlugin.instance().gson().fromJson(nextIdData, NextId.class);
            this.nextId = next.next();
            return AbstractFactionsPlugin.instance().gson().fromJson(content, new TypeToken<List<JSONFaction>>() {
            }.getType());
        }
    }

    public int getNextId() {
        while (!isIdFree(this.nextId)) {
            this.nextId += 1;
        }
        return this.nextId++;
    }

    public boolean isIdFree(int id) {
        return !this.factions.containsKey(id);
    }

    private synchronized void updateNextIdForId(int id) {
        if (this.nextId < id) {
            this.nextId = id + 1;
        }
    }

    @Override
    public MemoryFaction generateFactionObject(String tag) {
        int id = getNextId();
        MemoryFaction faction = new JSONFaction(id, tag);
        updateNextIdForId(id);
        return faction;
    }

    @Override
    public MemoryFaction generateFactionObject(int id, String tag) {
        MemoryFaction faction = new JSONFaction(id, tag);
        faction.resetPerms();
        return faction;
    }
}
