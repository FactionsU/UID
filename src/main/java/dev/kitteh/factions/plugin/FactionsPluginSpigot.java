package dev.kitteh.factions.plugin;

import dev.kitteh.factions.integration.IntegrationManager;

public class FactionsPluginSpigot extends AbstractFactionsPlugin {
    @Override
    public void onLoad() {
        IntegrationManager.onLoadFixSpigot(this);

        super.onLoad();
    }

    @Override
    protected String pluginType() {
        return "Spigot";
    }
}
