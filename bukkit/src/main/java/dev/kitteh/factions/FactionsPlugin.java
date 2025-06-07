package dev.kitteh.factions;

import com.google.gson.Gson;
import dev.kitteh.factions.config.ConfigManager;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.SeeChunkUtil;

public interface FactionsPlugin {
    static FactionsPlugin instance() {
        return AbstractFactionsPlugin.instance();
    }

    Gson gson();

    SeeChunkUtil seeChunkUtil();

    boolean autoSave();

    ConfigManager configManager();

    default MainConfig conf() {
        return this.configManager().mainConfig();
    }

    default TranslationsConfig tl() {
        return this.configManager().translationsConfig();
    }

    LandRaidControl landRaidControl();

    IntegrationManager integrationManager();
}
