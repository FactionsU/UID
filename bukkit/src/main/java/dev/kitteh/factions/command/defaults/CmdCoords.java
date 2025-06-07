package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Location;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdCoords implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("coords")
                        .commandDescription(Cloudy.desc(TL.COMMAND_COORDS_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.COORDS).and(Cloudy.hasFaction())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Location location = ((Sender.Player) context.sender()).player().getLocation();
        String message = TL.COMMAND_COORDS_MESSAGE.format(sender.describeToLegacy(sender.faction()), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getWorld().getName());
        for (FPlayer fPlayer : sender.faction().members()) {
            fPlayer.sendMessageLegacy(message);
        }
    }
}
