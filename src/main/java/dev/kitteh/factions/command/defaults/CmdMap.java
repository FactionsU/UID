package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.function.BiConsumer;


public class CmdMap implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("map")
                            .commandDescription(Cloudy.desc(TL.COMMAND_MAP_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MAP).and(Cloudy.isPlayer())))
                            .flag(manager.flagBuilder("auto-on"))
                            .flag(manager.flagBuilder("auto-off"))
                            .flag(manager.flagBuilder("setheight").withComponent(IntegerParser.integerParser(1, FactionsPlugin.getInstance().conf().map().getHeight() * 2)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        if (context.flags().get("setheight") instanceof Integer height) {
            sender.setMapHeight(height);
            sender.sendMessage(TL.COMMAND_MAPHEIGHT_SET.format(sender.getMapHeight()));
        }

        if (context.flags().hasFlag("auto-off")) {
            sender.setMapAutoUpdating(false);
            sender.msg(TL.COMMAND_MAP_UPDATE_DISABLED);
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostMap(), TL.COMMAND_MAP_TOSHOW, TL.COMMAND_MAP_FORSHOW)) {
            return;
        }

        if (context.flags().hasFlag("auto-on")) {
            sender.setMapAutoUpdating(true);
            sender.msg(TL.COMMAND_MAP_UPDATE_ENABLED);
        }

        for (Component component : ((MemoryBoard) Board.board()).getMap(sender, new FLocation(player), player.getLocation().getYaw())) {
            context.sender().sendMessage(component);
        }
    }
}
