package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.IntegerParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;


public class CmdMap implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("map")
                        .commandDescription(Cloudy.desc(TL.COMMAND_MAP_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MAP).and(Cloudy.isPlayer())))
                        .flag(manager.flagBuilder("auto-on"))
                        .flag(manager.flagBuilder("auto-off"))
                        .flag(manager.flagBuilder("setheight").withComponent(IntegerParser.integerParser(1, FactionsPlugin.instance().conf().map().getHeight() * 2)))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        if (context.flags().get("setheight") instanceof Integer height) {
            sender.mapHeight(height);
            sender.sendMessageLegacy(TL.COMMAND_MAPHEIGHT_SET.format(sender.mapHeight()));
        }

        if (context.flags().hasFlag("auto-off")) {
            sender.mapAutoUpdating(false);
            sender.msgLegacy(TL.COMMAND_MAP_UPDATE_DISABLED);
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostMap(), TL.COMMAND_MAP_TOSHOW, TL.COMMAND_MAP_FORSHOW)) {
            return;
        }

        if (context.flags().hasFlag("auto-on")) {
            sender.mapAutoUpdating(true);
            sender.msgLegacy(TL.COMMAND_MAP_UPDATE_ENABLED);
        }

        for (Component component : Instances.BOARD.getMap(sender, new FLocation(player), player.getLocation().getYaw())) {
            context.sender().sendMessage(component);
        }
    }
}
