package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.CmdGrace;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DurationParser;

import java.time.Duration;
import java.util.function.BiConsumer;

public class CmdSetGrace implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> graceBuilder = builder.literal("grace")
                    .commandDescription(Cloudy.desc(TL.COMMAND_SET_GRACE_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.GRACE_SET)));
            manager.command(graceBuilder.literal("off").handler(this::handleOff));
            manager.command(
                    graceBuilder.literal("on")
                            .required("duration", DurationParser.durationParser())
                            .handler(this::handleOn)
            );
        };
    }

    private void handleOff(CommandContext<Sender> context) {
        Universe.getInstance().setGraceRemaining(Duration.ZERO);
        context.sender().msg(TL.COMMAND_SET_GRACE_OFF);
    }

    private void handleOn(CommandContext<Sender> context) {
        Duration duration = context.get("duration");
        if (duration.isNegative() || duration.isZero()) {
            this.handleOff(context);
            return;
        }
        Universe.getInstance().setGraceRemaining(duration);
        context.sender().msg(TL.COMMAND_SET_GRACE_REMAINING_MESSAGE, CmdGrace.getGraceRemaining(duration));
    }
}
