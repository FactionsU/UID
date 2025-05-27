package dev.kitteh.factions;

import com.google.gson.Gson;
import dev.kitteh.factions.config.ConfigManager;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.SeeChunkUtil;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.UUID;

public interface FactionsPlugin {
    static FactionsPlugin instance() {
        return AbstractFactionsPlugin.instance();
    }

    Gson gson();

    SeeChunkUtil seeChunkUtil();

    boolean autoSave();

    ConfigManager configManager();

    MainConfig conf();

    TranslationsConfig tl();

    LandRaidControl landRaidControl();

    IntegrationManager integrationManager();
}
