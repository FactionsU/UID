package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdBypass implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
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
            sender.msg(TL.COMMAND_BYPASS_ENABLE);
            FactionsPlugin.instance().log(sender.name() + TL.COMMAND_BYPASS_ENABLELOG);
        } else {
            sender.msg(TL.COMMAND_BYPASS_DISABLE);
            FactionsPlugin.instance().log(sender.name() + TL.COMMAND_BYPASS_DISABLELOG);
        }
    }
}
