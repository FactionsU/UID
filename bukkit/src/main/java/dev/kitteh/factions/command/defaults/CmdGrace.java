package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.time.Duration;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdGrace implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("grace")
                        .commandDescription(Cloudy.desc(TL.COMMAND_GRACE_DESCRIPTION))
                        .permission(builder.commandPermission()
                                .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().protection().isGraceSystem()))
                                .and(Cloudy.hasPermission(Permission.GRACE_VIEW))
                        )
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Duration remaining = Universe.universe().graceRemaining();

        if (remaining.isZero()) {
            context.sender().msg(TL.COMMAND_GRACE_NOT_SET);
        } else {
            context.sender().msg(TL.COMMAND_GRACE_ACTIVE, MiscUtil.durationString(remaining));
        }
    }
}
