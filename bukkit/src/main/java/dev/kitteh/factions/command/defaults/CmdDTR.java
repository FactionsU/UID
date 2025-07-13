package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
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

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdDTR implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("dtr")
                        .commandDescription(Cloudy.desc(TL.COMMAND_DTR_DESCRIPTION))
                        .permission(builder.commandPermission().and(
                                Cloudy.predicate(s -> FactionsPlugin.instance().landRaidControl() instanceof DTRControl)
                                        .and(Cloudy.hasPermission(Permission.DTR))
                        ))
                        .optional("faction", FactionParser.of(FactionParser.Include.PLAYERS))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer fPlayer = context.sender().fPlayerOrNull();
        Faction faction = fPlayer != null && fPlayer.hasFaction() ? fPlayer.faction() : null;

        Faction target = context.getOrDefault("faction", faction);
        if (target == null) {
            return;
        }

        if (target != faction && !Permission.DTR_ANY.has(context.sender().sender())) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.sender().payForCommand(FactionsPlugin.instance().conf().economy().getCostDTR(), TL.COMMAND_DTR_TOSHOW, TL.COMMAND_DTR_FORSHOW)) {
            return;
        }

        DTRControl dtr = (DTRControl) FactionsPlugin.instance().landRaidControl();
        context.sender().msg(TL.COMMAND_DTR_DTR, target.describeToLegacy(fPlayer, false), DTRControl.round(target.dtr()), DTRControl.round(dtr.getMaxDTR(target)));
    }
}
