package dev.kitteh.factions.command.defaults.list;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.util.UUID;

public class CmdListInvites implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().list().invites();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SHOW_INVITES).and(Cloudy.hasFaction())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().list().invites();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Component component = Mini.parse(tl.getPending(), sender);
        for (UUID id : sender.faction().invites()) {
            FPlayer fp = FPlayers.fPlayers().get(id);
            String name = fp.name();
            component = component.append(Component.text().content(name + " ")
                    .hoverEvent(Mini.parse(tl.getClickToRevoke(), sender, FPlayerResolver.of("player", fp)).asHoverEvent())
                    .clickEvent(ClickEvent.runCommand("/" + Cmd.rootCommand() + " " + Confs.tl().commands().invite().getFirstAlias() + " " + name + " --delete"))
            );
        }

        context.sender().sendMessage(component);
    }
}
