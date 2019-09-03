package com.massivecraft.factions.config;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.DefaultOfflinePermissionsConfig;
import com.massivecraft.factions.config.file.DefaultPermissionsConfig;
import com.massivecraft.factions.config.file.DynmapConfig;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.config.transition.Transitioner;

import java.io.IOException;

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
        try {
            Loader.loadAndSave("default_permissions", this.permissionsConfig);
            Loader.loadAndSave("default_permissions_offline", this.offlinePermissionsConfig);
            Loader.loadAndSave("main", this.mainConfig);
            Loader.loadAndSave("dynmap", this.dynmapConfig);
        } catch (IOException | IllegalAccessException e) {
            e.printStackTrace();
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
