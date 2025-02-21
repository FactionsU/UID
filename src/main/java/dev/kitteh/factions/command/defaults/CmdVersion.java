package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdVersion implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("version")
                            .commandDescription(Cloudy.desc(TL.COMMAND_VERSION_DESCRIPTION))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        context.sender().sender().sendMessage(ChatColor.GREEN + FactionsPlugin.getInstance().getDescription().getFullName());
    }
}
