package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import java.time.Duration;

public class CmdGrace implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().grace();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission()
                                    .and(Cloudy.predicate(_ -> Confs.main().factions().protection().isGraceSystem()))
                                    .and(Cloudy.hasPermission(Permission.GRACE_VIEW))
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().grace();
        Duration remaining = Universe.universe().graceRemaining();

        if (remaining.isZero()) {
            context.sender().sendRichMessage(tl.getNotSet());
        } else {
            context.sender().sendRichMessage(tl.getActive(), Placeholder.unparsed("duration", MiscUtil.durationString(remaining)));
        }
    }
}
