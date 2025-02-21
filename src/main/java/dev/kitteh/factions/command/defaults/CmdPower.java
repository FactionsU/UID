package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.landraidcontrol.PowerControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdPower implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("power")
                            .commandDescription(Cloudy.desc(TL.COMMAND_POWER_DESCRIPTION))
                            .permission(builder.commandPermission().and(
                                    Cloudy.predicate(s -> FactionsPlugin.getInstance().getLandRaidControl() instanceof PowerControl)
                                            .and(Cloudy.hasPermission(Permission.POWER))
                            ))
                            .optional("player", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = context.sender().fPlayerOrNull();

        FPlayer target = context.getOrDefault("player", sender);
        if (target == null) {
            return;
        }

        if (target != sender && !Permission.POWER_ANY.has(context.sender().sender(), true)) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostPower(), TL.COMMAND_POWER_TOSHOW, TL.COMMAND_POWER_FORSHOW)) {
            return;
        }

        double powerBoost = target.getPowerBoost();
        String boost = (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_POWER_BONUS.toString() : TL.COMMAND_POWER_PENALTY.toString()) + powerBoost + ")";
        context.sender().msg(TL.COMMAND_POWER_POWER, target.describeTo(sender), target.getPowerRounded(), target.getPowerMaxRounded(), boost);
    }
}
