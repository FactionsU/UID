package com.massivecraft.factions.data.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.data.MemoryFPlayer;
import com.massivecraft.factions.data.MemoryFPlayers;
import com.massivecraft.factions.util.DiscUtil;
import com.massivecraft.factions.util.OldJSONFPlayerDeserializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JSONFPlayers extends MemoryFPlayers {
    public Gson getGson() {
        return FactionsPlugin.getInstance().getGson();
    }

    @Deprecated
    public void setGson(Gson gson) {
        // NOOP
    }

    private final File file;

    public JSONFPlayers() {
        if (FactionsPlugin.getInstance().getServerUUID() == null) {
            FactionsPlugin.getInstance().grumpException(new RuntimeException());
        }
        file = new File(FactionsPlugin.getInstance().getDataFolder(), "data/players.json");
    }

    @Deprecated
    public void convertFrom(MemoryFPlayers old) {
        old.fPlayers.forEach((id, faction) -> this.fPlayers.put(id, new JSONFPlayer((MemoryFPlayer) faction)));
        forceSave();
        FPlayers.instance = this;
    }

    public void forceSave() {
        forceSave(true);
    }

    public void forceSave(boolean sync) {
        final List<JSONFPlayer> entitiesThatShouldBeSaved = new ArrayList<>();
        boolean saveAll = FactionsPlugin.getInstance().conf().data().json().isSaveAllPlayers();
        for (FPlayer entity : this.fPlayers.values()) {
            if (saveAll || ((MemoryFPlayer) entity).shouldBeSaved()) {
                entitiesThatShouldBeSaved.add((JSONFPlayer) entity);
            }
        }

        saveCore(file, entitiesThatShouldBeSaved, sync);
    }

    private boolean saveCore(File target, List<JSONFPlayer> data, boolean sync) {
        return DiscUtil.writeCatch(target, FactionsPlugin.getInstance().getGson().toJson(data), sync);
    }

    public int load() {
        List<JSONFPlayer> fplayers = this.loadCore();
        if (fplayers == null) {
            return 0;
        }
        this.fPlayers.clear();
        fplayers.forEach(fp -> this.fPlayers.put(fp.getId(), fp));
        return fPlayers.size();
    }

    private List<JSONFPlayer> loadCore() {
        if (!this.file.exists()) {
            return null;
        }

        String content = DiscUtil.readCatch(this.file);
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        if (content.startsWith("{")) {
            Gson gson = FactionsPlugin.getInstance().getGsonBuilder(false)
                    .registerTypeAdapter(JSONFaction.class, new OldJSONFPlayerDeserializer())
                    .create();

            Map<String, JSONFPlayer> map = gson.fromJson(content, new TypeToken<Map<String, JSONFPlayer>>() {
            }.getType());
            return new ArrayList<>(map.values());
        } else {
            return FactionsPlugin.getInstance().getGson().fromJson(content, new TypeToken<List<JSONFPlayer>>() {
            }.getType());
        }
    }

    private boolean doesKeyNeedMigration(String key) {
        if (!key.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            // Not a valid UUID..
            // Valid playername, we'll mark this as one for conversion
            // to UUID
            return key.matches("[a-zA-Z0-9_]{2,16}");
        }
        return false;
    }

    private boolean isKeyInvalid(String key) {
        return !key.matches("[a-zA-Z0-9_]{2,16}");
    }

    @Override
    public FPlayer generateFPlayer(String id) {
        FPlayer player = new JSONFPlayer(id);
        this.fPlayers.put(player.getId(), player);
        return player;
    }
}
