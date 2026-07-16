package dev.kitteh.factions.command.paper;

import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.context.CommandContext;

public class CmdVersion extends dev.kitteh.factions.command.defaults.CmdVersion {
    @Override
    protected void handle(CommandContext<Sender> context) {
        context.sender().sendMessage(Component.textOfChildren(
                Component.text(AbstractFactionsPlugin.instance().getPluginMeta().getName(), Confs.tl().colors().info()),
                Component.text(" "),
                Component.text(AbstractFactionsPlugin.instance().getPluginMeta().getVersion(), Confs.tl().colors().focus())
        ));
    }
}
