package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.time.Duration;
import java.util.function.BiConsumer;

public class CmdGrace implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("grace")
                            .commandDescription(Cloudy.desc(TL.COMMAND_GRACE_DESCRIPTION))
                            .permission(builder.commandPermission()
                                    .and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().factions().protection().isGraceSystem()))
                                    .and(Cloudy.hasPermission(Permission.GRACE_VIEW))
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        Duration remaining = Universe.universe().graceRemaining();

        if (remaining.isZero()) {
            context.sender().msg(TL.COMMAND_GRACE_NOT_SET);
        } else {
            context.sender().msg(TL.COMMAND_GRACE_REMAINING_MESSAGE, getGraceRemaining(remaining));
        }
    }

    public static String getGraceRemaining(Duration remaining) {
        long days = remaining.toDays();
        long hours = remaining.toHoursPart();
        long minutes = remaining.toMinutesPart();
        long seconds = remaining.toSecondsPart();

        StringBuilder builder = new StringBuilder();
        if (days > 0) {
            builder.append(TL.COMMAND_GRACE_REMAINING_DAYS.format(days)).append(", ");
        }
        if (hours > 0) {
            builder.append(TL.COMMAND_GRACE_REMAINING_HOURS.format(hours)).append(", ");
        }
        if (minutes > 0) {
            builder.append(TL.COMMAND_GRACE_REMAINING_MINUTES.format(minutes)).append(", ");
        }
        if (!builder.isEmpty()) {
            builder.append("and ");
        }
        builder.append(TL.COMMAND_GRACE_REMAINING_SECONDS.format(seconds));
        return builder.toString();
    }
}
