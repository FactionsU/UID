package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.MiscUtil;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DurationParser;

import java.time.Duration;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetGrace implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().set().grace();
            Command.Builder<Sender> graceBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
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
        var tl = FactionsPlugin.instance().tl().commands().admin().set().grace();
        Universe.universe().graceRemaining(Duration.ZERO);
        context.sender().sendRichMessage(tl.getInactive());
    }

    private void handleOn(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().admin().set().grace();
        Duration duration = context.get("duration");
        if (duration.isNegative() || duration.isZero()) {
            this.handleOff(context);
            return;
        }
        Universe.universe().graceRemaining(duration);
        context.sender().sendRichMessage(tl.getActive(), Placeholder.unparsed("duration", MiscUtil.durationString(duration)));
    }
}
