package dev.kitteh.factions.cmd.dtr;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdDTRGet extends FCommand {
    public CmdDTRGet() {
        super();
        this.aliases.add("get");
        this.optionalArgs.put("faction", "yours");

        this.requirements = new CommandRequirements.Builder(Permission.DTR).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        Faction target = context.argAsFaction(0, context.faction);
        if (target == null) {
            return;
        }

        if (target != context.faction && !Permission.DTR_ANY.has(context.sender, true)) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostDTR(), TL.COMMAND_DTR_TOSHOW, TL.COMMAND_DTR_FORSHOW)) {
            return;
        }

        DTRControl dtr = (DTRControl) FactionsPlugin.getInstance().getLandRaidControl();
        context.msg(TL.COMMAND_DTR_DTR, target.describeTo(context.fPlayer, false), DTRControl.round(target.getDTR()), DTRControl.round(dtr.getMaxDTR(target)));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DTR_DESCRIPTION;
    }

}
