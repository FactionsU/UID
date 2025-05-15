package dev.kitteh.factions.plugin;

import dev.kitteh.factions.integration.IntegrationManager;

public class FactionsPluginSpigot extends AbstractFactionsPlugin {
    @Override
    public void onLoad() {
        IntegrationManager.onLoadFixSpigot(this);
        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
        } catch (Exception ignored) {
            // eh
        }
        super.onLoad();
    }
}
