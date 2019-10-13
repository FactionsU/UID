package com.massivecraft.factions.config;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.config.file.DefaultOfflinePermissionsConfig;
import com.massivecraft.factions.config.file.DefaultPermissionsConfig;
import com.massivecraft.factions.config.file.MainConfig;
import com.massivecraft.factions.config.transition.Transitioner;

import java.io.IOException;

public class ConfigManager {
    private final FactionsPlugin plugin;
    private final DefaultPermissionsConfig permissionsConfig = new DefaultPermissionsConfig();
    private final DefaultOfflinePermissionsConfig offlinePermissionsConfig = new DefaultOfflinePermissionsConfig();
    private final MainConfig mainConfig = new MainConfig();
    private static final String mainConfigComment = "# FactionsUUID by drtshock\n" +
            "# Report issues https://github.com/drtshock/Factions/issues?state=open\n" +
            "# Live support http://factions-support.cf\n" +
            "# Website https://www.spigotmc.org/resources/factionsuuid.1035/\n" +
            "\n" +
            "# Made with love <3";

    public ConfigManager(FactionsPlugin plugin) {
        this.plugin = plugin;
    }

    public void startup() {
        if (!this.plugin.getDataFolder().toPath().resolve("config").toFile().exists()) {
            Transitioner transitioner = new Transitioner(this.plugin);
            transitioner.migrateV0();
        }
        this.loadConfigs();
    }

    public void loadConfigs() {
        try {
            Loader.loadAndSave("default_permissions", this.permissionsConfig);
            Loader.loadAndSave("default_permissions_offline", this.offlinePermissionsConfig);
            Loader.loadAndSave("main", this.mainConfig);
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
}
