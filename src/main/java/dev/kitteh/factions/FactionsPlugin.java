package dev.kitteh.factions;

import com.google.gson.Gson;
import dev.kitteh.factions.config.ConfigManager;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.SeeChunkUtil;
import dev.kitteh.factions.util.particle.BukkitParticleProvider;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public interface FactionsPlugin {
    static FactionsPlugin getInstance() {
        return AbstractFactionsPlugin.getInstance();
    }

    Gson getGson();

    SeeChunkUtil getSeeChunkUtil();

    BukkitParticleProvider getParticleProvider();

    Map<UUID, Integer> getStuckMap();

    Map<UUID, Long> getTimers();

    void log(String msg);

    void log(String str, Object... args);

    void log(Level level, String str, Object... args);

    void log(Level level, String msg);

    boolean getAutoSave();

    void setAutoSave(boolean val);

    ConfigManager getConfigManager();

    MainConfig conf();

    TranslationsConfig tl();

    LandRaidControl getLandRaidControl();

    void debug(Level level, String s);

    void debug(String s);

    OfflinePlayer getFactionOfflinePlayer(String name);

    IntegrationManager getIntegrationManager();
}
