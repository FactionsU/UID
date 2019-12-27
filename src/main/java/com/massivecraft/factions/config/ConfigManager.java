package com.massivecraft.factions.config;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.DefaultOfflinePermissionsConfig;
import com.massivecraft.factions.config.file.DefaultPermissionsConfig;
import com.massivecraft.factions.config.file.DynmapConfig;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.config.transition.Transitioner;

import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    private final FactionsPlugin plugin;
    private final DefaultPermissionsConfig permissionsConfig = new DefaultPermissionsConfig();
    private final DefaultOfflinePermissionsConfig offlinePermissionsConfig = new DefaultOfflinePermissionsConfig();
    private final MainConfig mainConfig = new MainConfig();
    private final DynmapConfig dynmapConfig = new DynmapConfig();

    public ConfigManager(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void startup() {
        Transitioner.transition(this.plugin);
        this.loadConfigs();
    }

    public void loadConfigs() {
        this.loadConfig("default_permissions", this.permissionsConfig);
        this.loadConfig("default_permissions_offline", this.offlinePermissionsConfig);
        this.loadConfig("main", this.mainConfig);
        this.loadConfig("dynmap", this.dynmapConfig);
    }

    private void loadConfig(String name, Object config) {
        try {
            Loader.loadAndSave(name, config);
        } catch (IOException | IllegalAccessException e) {
            FactionsPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not load config '" + name + ".conf'", e);
        }
    }

    public DefaultPermissionsConfig getPermissionsConfig() {
        return this.permissionsConfig;
    }

    public DefaultOfflinePermissionsConfig getOfflinePermissionsConfig() {
        return this.offlinePermissionsConfig;
    }

    public MainConfig getMainConfig() {
        return this.mainConfig;
    }

    public DynmapConfig getDynmapConfig() {
        return dynmapConfig;
    }
}
