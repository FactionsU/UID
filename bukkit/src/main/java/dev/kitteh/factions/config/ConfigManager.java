package dev.kitteh.factions.config;

import dev.kitteh.factions.config.file.DynmapConfig;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.config.file.PermissionsConfig;
import dev.kitteh.factions.config.file.TranslationsConfig;
import dev.kitteh.factions.config.transition.Transitioner;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;

import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    private final AbstractFactionsPlugin plugin;
    private PermissionsConfig permissionsConfig = null;
    private final MainConfig mainConfig = new MainConfig();
    private final DynmapConfig dynmapConfig = new DynmapConfig();
    private final TranslationsConfig translationsConfig = new TranslationsConfig();

    public ConfigManager(AbstractFactionsPlugin plugin) {
        this.plugin = plugin;
        Transitioner.transition(this.plugin);
    }

    public void loadConfigs() {
        if (this.permissionsConfig == null) {
            this.permissionsConfig = new PermissionsConfig();
        }

        this.loadConfig("translations", this.translationsConfig);
        this.loadConfig("permissions", this.permissionsConfig);
        this.loadConfig("main", this.mainConfig);
        this.loadConfig("dynmap", this.dynmapConfig);
    }

    private void loadConfig(String name, Object config) {
        try {
            Loader.loadAndSave(name, config);
        } catch (IOException | IllegalAccessException e) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not load config '" + name + ".conf'", e);
        }
    }

    public PermissionsConfig permissionsConfig() {
        return this.permissionsConfig;
    }

    public MainConfig mainConfig() {
        return this.mainConfig;
    }

    public DynmapConfig dynmapConfig() {
        return this.dynmapConfig;
    }

    public TranslationsConfig translationsConfig() {
        return this.translationsConfig;
    }
}
