package dev.kitteh.factions.command.defaults.admin.dtr;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import java.util.function.BiConsumer;

public class CmdDTRSet implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("set")
                        .commandDescription(Cloudy.desc(TL.COMMAND_DTR_MODIFY_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MODIFY_DTR)))
                        .required("faction", FactionParser.of(FactionParser.Include.SELF))
                        .required("amount", DoubleParser.doubleParser(0))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Faction target = context.get("faction");

        double amount = context.get("amount");

        DTRControl dtr = (DTRControl) FactionsPlugin.instance().landRaidControl();
        target.dtr(Math.max(Math.min(amount, dtr.getMaxDTR(target)), FactionsPlugin.instance().conf().factions().landRaidControl().dtr().getMinDTR()));
        context.sender().msg(TL.COMMAND_DTR_MODIFY_DONE, target.describeTo(context.sender().fPlayerOrNull(), false), DTRControl.round(target.dtr()));
    }
}
