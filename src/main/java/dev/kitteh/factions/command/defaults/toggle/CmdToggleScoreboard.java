package dev.kitteh.factions.command.defaults.toggle;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.scoreboard.FScoreboard;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdToggleScoreboard implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("scoreboard")
                        .commandDescription(Cloudy.desc(TL.COMMAND_SCOREBOARD_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SCOREBOARD).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        boolean toggleTo = !sender.showScoreboard();
        FScoreboard board = FScoreboard.get(sender);
        if (board == null) {
            sender.msg(TL.COMMAND_TOGGLESB_DISABLED);
        } else {
            sender.sendMessage(TL.TOGGLE_SB.toString().replace("{value}", String.valueOf(toggleTo)));
            board.setSidebarVisibility(toggleTo);
        }
        sender.showScoreboard(toggleTo);
    }
}
