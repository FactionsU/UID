package dev.kitteh.factions.data.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.data.MemoryFPlayer;
import dev.kitteh.factions.data.MemoryFPlayers;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.adapter.OldJSONFPlayerDeserializer;
import org.bukkit.entity.Player;
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
    private final Path playersPath = AbstractFactionsPlugin.instance().getDataFolder().toPath().resolve("data/players.json");

    public JSONFPlayers() {
        if (AbstractFactionsPlugin.instance().getServerUUID() == null) {
            AbstractFactionsPlugin.instance().grumpException(new RuntimeException());
        }
    }

    @Override
    public void forceSave(boolean sync) {
        final List<JSONFPlayer> entitiesThatShouldBeSaved = new ArrayList<>();
        boolean saveAll = FactionsPlugin.instance().conf().data().json().isSaveAllPlayers();
        for (FPlayer entity : this.fPlayers.values()) {
            if (saveAll || ((MemoryFPlayer) entity).shouldBeSaved()) {
                entitiesThatShouldBeSaved.add((JSONFPlayer) entity);
            }
        }

        JsonSaver.write(playersPath, () -> FactionsPlugin.instance().gson().toJson(entitiesThatShouldBeSaved), sync);
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
        if (!Files.exists(playersPath)) {
            return null;
        }

        try (BufferedReader bufferedReader = Files.newBufferedReader(playersPath)) {
            int len = 50;
            char[] chars = new char[len];
            bufferedReader.mark(len + 1);
            int read = bufferedReader.read(chars, 0, len);
            if (read < 40) {
                AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "JSON players were less than 40 chars in length???");
                return null;
            }
            bufferedReader.reset();
            if (new String(chars).trim().startsWith("{")) {
                Gson gson = AbstractFactionsPlugin.instance().getGsonBuilder(false)
                        .registerTypeAdapter(JSONFaction.class, new OldJSONFPlayerDeserializer())
                        .create();

                Map<String, JSONFPlayer> map = gson.fromJson(bufferedReader, new TypeToken<Map<String, JSONFPlayer>>() {
                }.getType());
                return new ArrayList<>(map.values());
            } else {
                return FactionsPlugin.instance().gson().fromJson(bufferedReader, new TypeToken<List<JSONFPlayer>>() {
                }.getType());
            }
        } catch (IOException e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to load JSON players", e);
            return null;
        }
    }

    @Override
    public FPlayer constructNewFPlayer(UUID id) {
        FPlayer fp = new JSONFPlayer(Objects.requireNonNull(id));
        if (fp.asPlayer() instanceof Player player) {
            player.updateCommands();
        }
        return fp;
    }

    @Override
    public void removePlayer(FPlayer fPlayer) {
        this.fPlayers.remove(fPlayer.uniqueId());
    }
}
