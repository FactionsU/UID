package dev.kitteh.factions.data.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.MemoryFPlayer;
import dev.kitteh.factions.data.MemoryFPlayers;
import dev.kitteh.factions.util.DiscUtil;
import dev.kitteh.factions.util.adapter.OldJSONFPlayerDeserializer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@NullMarked
public final class JSONFPlayers extends MemoryFPlayers {
    private final File file;

    public JSONFPlayers() {
        if (FactionsPlugin.getInstance().getServerUUID() == null) {
            FactionsPlugin.getInstance().grumpException(new RuntimeException());
        }
        file = new File(FactionsPlugin.getInstance().getDataFolder(), "data/players.json");
    }

    @Override
    public void forceSave(boolean sync) {
        final List<JSONFPlayer> entitiesThatShouldBeSaved = new ArrayList<>();
        boolean saveAll = FactionsPlugin.getInstance().conf().data().json().isSaveAllPlayers();
        for (FPlayer entity : this.fPlayers.values()) {
            if (saveAll || ((MemoryFPlayer) entity).shouldBeSaved()) {
                entitiesThatShouldBeSaved.add((JSONFPlayer) entity);
            }
        }

        DiscUtil.writeCatch(file, FactionsPlugin.getInstance().getGson().toJson(entitiesThatShouldBeSaved), sync);
    }

    @Override
    public int load() {
        List<JSONFPlayer> fplayers = this.loadCore();
        if (fplayers == null) {
            return 0;
        }
        this.fPlayers.clear();
        fplayers.forEach(fp -> this.fPlayers.put(fp.getUniqueId(), fp));
        return fPlayers.size();
    }

    private @Nullable List<JSONFPlayer> loadCore() {
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

    @Override
    public FPlayer constructNewFPlayer(UUID id) {
        return new JSONFPlayer(Objects.requireNonNull(id));
    }

    @Override
    public void removePlayer(FPlayer fPlayer) {
        this.fPlayers.remove(fPlayer.getUniqueId());
    }
}
