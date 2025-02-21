package dev.kitteh.factions.command.defaults.admin.power;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import java.util.function.BiConsumer;

public class CmdPowerBoost implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> boostBuilder = builder.literal("boost")
                    .commandDescription(Cloudy.desc(TL.COMMAND_POWERBOOST_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.POWERBOOST)));

            Command.Builder<Sender> boostSetBuilder = boostBuilder.literal("set");
            Command.Builder<Sender> boostModifyBuilder = boostBuilder.literal("modify");

            manager.command(
                    boostSetBuilder.literal("faction")
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handleFaction(ctx, false))
            );
            manager.command(
                    boostSetBuilder.literal("player")
                            .required("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handlePlayer(ctx, false))
            );

            manager.command(
                    boostModifyBuilder.literal("faction")
                            .required("faction", FactionParser.of(FactionParser.Include.SELF))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handleFaction(ctx, true))
            );
            manager.command(
                    boostModifyBuilder.literal("player")
                            .required("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .required("value", DoubleParser.doubleParser())
                            .handler(ctx -> this.handlePlayer(ctx, true))
            );
        };
    }

    private void handlePlayer(CommandContext<Sender> context, boolean modify) {
        FPlayer target = context.get("player");
        double value = context.get("value");

        if (modify) {
            value += target.getPowerBoost();
        }

        target.setPowerBoost(value);

        context.sender().msg(TL.COMMAND_POWERBOOST_BOOST, target, Math.round(value));
    }

    private void handleFaction(CommandContext<Sender> context, boolean modify) {
        Faction target = context.get("faction");
        double value = context.get("value");

        if (modify) {
            value += target.getPowerBoost();
        }

        target.setPowerBoost(value);

        context.sender().msg(TL.COMMAND_POWERBOOST_BOOST, target, Math.round(value));
    }
}
