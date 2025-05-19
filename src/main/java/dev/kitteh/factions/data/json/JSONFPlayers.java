package dev.kitteh.factions.data.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.MemoryFPlayer;
import dev.kitteh.factions.data.MemoryFPlayers;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.adapter.OldJSONFPlayerDeserializer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@NullMarked
public final class JSONFPlayers extends MemoryFPlayers {
    private final Path path = AbstractFactionsPlugin.getInstance().getDataFolder().toPath().resolve("data/players.json");

    public JSONFPlayers() {
        if (AbstractFactionsPlugin.getInstance().getServerUUID() == null) {
            AbstractFactionsPlugin.getInstance().grumpException(new RuntimeException());
        }
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

        JsonSaver.write(path, () -> FactionsPlugin.getInstance().getGson().toJson(entitiesThatShouldBeSaved), sync);
    }

    @Override
    public int load() {
        List<JSONFPlayer> fplayers = this.loadCore();
        if (fplayers == null) {
            return 0;
        }
        this.fPlayers.clear();
        fplayers.forEach(fp -> this.fPlayers.put(fp.uniqueId(), fp));
        return fPlayers.size();
    }

    private @Nullable List<JSONFPlayer> loadCore() {
        if (!Files.exists(path)) {
            return null;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
            int len = 50;
            char[] chars = new char[len];
            bufferedReader.mark(len + 1);
            int read = bufferedReader.read(chars, 0, len);
            if (read < 40) {
                AbstractFactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "JSON players were less than 40 chars in length???");
                return null;
            }
            bufferedReader.reset();
            if (new String(chars).trim().startsWith("{")) {
                Gson gson = AbstractFactionsPlugin.getInstance().getGsonBuilder(false)
                        .registerTypeAdapter(JSONFaction.class, new OldJSONFPlayerDeserializer())
                        .create();

                Map<String, JSONFPlayer> map = gson.fromJson(bufferedReader, new TypeToken<Map<String, JSONFPlayer>>() {
                }.getType());
                return new ArrayList<>(map.values());
            } else {
                return FactionsPlugin.getInstance().getGson().fromJson(bufferedReader, new TypeToken<List<JSONFPlayer>>() {
                }.getType());
            }
        } catch (IOException e) {
            AbstractFactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to load JSON players", e);
            return null;
        }
    }

    @Override
    public FPlayer constructNewFPlayer(UUID id) {
        return new JSONFPlayer(Objects.requireNonNull(id));
    }

    @Override
    public void removePlayer(FPlayer fPlayer) {
        this.fPlayers.remove(fPlayer.uniqueId());
    }
}
