package dev.kitteh.factions;

import com.google.gson.Gson;
import dev.kitteh.factions.config.ConfigManager;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.SeeChunkUtil;
import dev.kitteh.factions.util.particle.ParticleProvider;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public interface FactionsPlugin {
    static FactionsPlugin instance() {
        return AbstractFactionsPlugin.getInstance();
    }

    Gson gson();

    SeeChunkUtil seeChunkUtil();

    Map<UUID, Integer> stuckMap();

    Map<UUID, Long> timers();

    void log(String msg);

    void log(String str, Object... args);

    void log(Level level, String str, Object... args);

    void log(Level level, String msg);

    boolean autoSave();

    void autoSave(boolean val);

    ConfigManager configManager();

    MainConfig conf();

    TranslationsConfig tl();

    LandRaidControl landRaidControl();

    void debug(Level level, String s);

    void debug(String s);

    OfflinePlayer factionOfflinePlayer(String name);

    IntegrationManager integrationManager();
}
