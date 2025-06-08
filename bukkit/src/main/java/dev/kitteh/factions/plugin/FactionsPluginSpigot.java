package dev.kitteh.factions.plugin;

import dev.kitteh.factions.integration.IntegrationManager;

public class FactionsPluginSpigot extends AbstractFactionsPlugin {
    @Override
    public void onPluginLoad() {
        IntegrationManager.onLoadFixSpigot(this);
    }

    @Override
    protected String pluginType() {
        return "Spigot";
    }
}
