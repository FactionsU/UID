package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdBypass implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("bypass")
                        .commandDescription(Cloudy.desc(TL.COMMAND_BYPASS_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BYPASS).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        sender.adminBypass(!sender.adminBypass());

        if (sender.adminBypass()) {
            sender.msgLegacy(TL.COMMAND_BYPASS_ENABLE);
            AbstractFactionsPlugin.instance().log(sender.name() + TL.COMMAND_BYPASS_ENABLELOG);
        } else {
            sender.msgLegacy(TL.COMMAND_BYPASS_DISABLE);
            AbstractFactionsPlugin.instance().log(sender.name() + TL.COMMAND_BYPASS_DISABLELOG);
        }
    }
}
