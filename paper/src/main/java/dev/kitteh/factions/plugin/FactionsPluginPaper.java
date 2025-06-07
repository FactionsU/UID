package dev.kitteh.factions.plugin;

import dev.kitteh.factions.util.ComponentDispatcher;
import moss.factions.shade.net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class FactionsPluginPaper extends AbstractFactionsPlugin {
    @Override
    protected String pluginType() {
        return "fully-featured Paper";
    }

    @Override
    public void onLoad() {
        ComponentDispatcher.setSenders(
                (commandSender, component) -> {
                    commandSender.sendMessage(getComponent(component));
                },
                (player, component) -> {
                    player.sendActionBar(getComponent(component));
                }
        );
        super.onLoad();
    }

    private Component getComponent(ComponentLike component) {
        return GsonComponentSerializer.gson().deserializeFromTree(moss.factions.shade.net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson().serializeToTree(component.asComponent()));
    }
}
