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

public class CmdToggleSeeChunk implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("seechunk")
                        .commandDescription(Cloudy.desc(TL.COMMAND_SEECHUNK_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SEECHUNK).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        boolean toggle = !sender.seeChunk();
        sender.seeChunk(toggle);
        sender.msg(TL.COMMAND_SEECHUNK_TOGGLE, toggle ? "enabled" : "disabled");
    }
}
