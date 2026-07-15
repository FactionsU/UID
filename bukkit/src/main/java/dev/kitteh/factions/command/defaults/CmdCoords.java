package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Location;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdCoords implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().coords();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.COORDS).and(Cloudy.hasFaction())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().coords();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Location location = ((Sender.Player) context.sender()).player().getLocation();

        for (FPlayer fPlayer : sender.faction().members()) {
            fPlayer.sendRichMessage(tl.getMessage(),
                    FPlayerResolver.of("player", sender),
                    Placeholder.unparsed("x", String.valueOf(location.getBlockX())),
                    Placeholder.unparsed("y", String.valueOf(location.getBlockY())),
                    Placeholder.unparsed("z", String.valueOf(location.getBlockZ())),
                    Placeholder.unparsed("world", location.getWorld().getName()));
        }
    }
}
