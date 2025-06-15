package dev.kitteh.factions.plugin;

import dev.kitteh.factions.util.ComponentDispatcher;
import net.kyori.adventure.audience.Audience;

public class FactionsPluginPaper extends AbstractFactionsPlugin {
    @Override
    protected String pluginType() {
        return "fully-featured Paper";
    }

    @Override
    public void onPluginLoad() {
        ComponentDispatcher.setSenders(
                (commandSender, component) -> commandSender.sendMessage(component),
                Audience::sendActionBar
        );
    }
}
