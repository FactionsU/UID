package dev.kitteh.factions.data.json;

import com.google.gson.Gson;
import dev.kitteh.factions.data.MemoryUniverse;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import org.jspecify.annotations.NullMarked;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

@NullMarked
public final class JSONUniverse extends MemoryUniverse {
    private final Path path = AbstractFactionsPlugin.instance().getDataFolder().toPath().resolve("data/universe.json");

    private final Gson gson = AbstractFactionsPlugin.instance().getGsonBuilder(false).setPrettyPrinting().create();

    @Override
    public void forceSave(boolean sync) {
        String data = this.gson.toJson(this.data);
        JsonSaver.write(path, () -> data, sync);
    }

    @Override
    public void loadData() {
        if (!Files.exists(path)) {
            AbstractFactionsPlugin.instance().getLogger().info("No universe to load from disk. Creating new file.");
            forceSave(true);
        }

        try {
            this.data = this.gson.fromJson(Files.newBufferedReader(path), MemoryUniverse.Data.class);
        } catch (Exception e) {
            AbstractFactionsPlugin.instance().getLogger().log(Level.SEVERE, "Failed to load the universe from disk.", e);
        }
    }
}
