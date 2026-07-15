package dev.kitteh.factions;

import com.google.gson.Gson;
import dev.kitteh.factions.config.ConfigManager;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.integration.IntegrationManager;
import dev.kitteh.factions.landraidcontrol.LandRaidControl;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.SeeChunkUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.AvailableSince("4.0.0")
@ApiStatus.NonExtendable
@NullMarked
public interface FactionsPlugin {
    static FactionsPlugin instance() {
        return AbstractFactionsPlugin.instance();
    }

    boolean autoSave();

    LandRaidControl landRaidControl();

    @Deprecated(forRemoval = true, since = "4.3.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    IntegrationManager integrationManager();

    @Deprecated(forRemoval = true, since = "4.3.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    Gson gson();

    @Deprecated(forRemoval = true, since = "4.7.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    SeeChunkUtil seeChunkUtil();

    @Deprecated(forRemoval = true, since = "4.7.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    ConfigManager configManager();

    @Deprecated(forRemoval = true, since = "4.7.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    default MainConfig conf() {
        return Confs.main();
    }

    @Deprecated(forRemoval = true, since = "4.7.0")
    @ApiStatus.ScheduledForRemoval(inVersion = "5.0.0")
    default TranslationsConfig tl() {
        return Confs.tl();
    }
}
