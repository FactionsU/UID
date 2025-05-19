package dev.kitteh.factions.command.defaults.toggle;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdToggleLogins implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("logins")
                            .commandDescription(Cloudy.desc(TL.COMMAND_LOGINS_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MONITOR_LOGINS).and(Cloudy.hasFaction())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        boolean monitor = sender.monitorJoins();
        sender.msg(TL.COMMAND_LOGINS_TOGGLE, String.valueOf(!monitor));
        sender.monitorJoins(!monitor);
    }
}
