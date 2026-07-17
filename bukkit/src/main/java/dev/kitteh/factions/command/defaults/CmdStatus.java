package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdStatus implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        var tl = Confs.tl().commands().status();
        return (manager, builder, _) -> manager.command(
                builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                        .commandDescription(Cloudy.desc(tl.getDescription()))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.STATUS).and(Cloudy.hasFaction())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().status();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        for (FPlayer fp : sender.faction().members()) {
            sender.sendRichMessage(tl.getFormat(), FPlayerResolver.of("player", fp));
        }
    }
}
