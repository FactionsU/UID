package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
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
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().map();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MAP).and(Cloudy.isPlayer())))
                            .flag(manager.flagBuilder("auto-on").withPermission(Cloudy.hasPermission(Permission.MAP_AUTO)))
                            .flag(manager.flagBuilder("auto-off").withPermission(Cloudy.hasPermission(Permission.MAP_AUTO)))
                            .flag(manager.flagBuilder("set-height").withComponent(IntegerParser.integerParser(1, FactionsPlugin.instance().conf().map().getHeight() * 2)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Player player = ((Sender.Player) context.sender()).player();

        var tl = FactionsPlugin.instance().tl().commands().map();
        var econTl = FactionsPlugin.instance().tl().economy().actions();

        if (context.flags().get("set-height") instanceof Integer height) {
            sender.mapHeight(height);
            sender.sendRichMessage(tl.getHeightSet(), Placeholder.unparsed("lines", String.valueOf(sender.mapHeight())));
        }

        if (context.flags().hasFlag("auto-off")) {
            sender.mapAutoUpdating(false);
            sender.sendRichMessage(tl.getUpdateDisabled());
            return;
        }

        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostMap(), econTl.getMapTo(), econTl.getMapFor())) {
            return;
        }

        if (context.flags().hasFlag("auto-on")) {
            sender.mapAutoUpdating(true);
            sender.sendRichMessage(tl.getUpdateEnabled());
        }

        for (Component component : Instances.BOARD.getMap(sender, new FLocation(player), player.getLocation().getYaw())) {
            context.sender().sendMessage(component);
        }
    }
}
