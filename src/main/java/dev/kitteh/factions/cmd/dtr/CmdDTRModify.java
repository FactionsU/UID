package dev.kitteh.factions.cmd.dtr;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdDTRModify extends FCommand {

    public CmdDTRModify() {
        super();
        this.aliases.add("modify");
        this.requiredArgs.add("faction");
        this.requiredArgs.add("amount");

        this.requirements = new CommandRequirements.Builder(Permission.MODIFY_DTR).build();
    }

    @Override
    public void perform(CommandContext context) {
        Faction target = context.argAsFaction(0, null);
        if (target == null) {
            return;
        }

        double amount = context.argAsDouble(1, 0.0D);
        if (amount == 0.0D) {
            return;
        }

        DTRControl dtr = (DTRControl) FactionsPlugin.getInstance().getLandRaidControl();
        target.setDTR(Math.max(Math.min(target.getDTR() + amount, dtr.getMaxDTR(target)), FactionsPlugin.getInstance().conf().factions().landRaidControl().dtr().getMinDTR()));
        context.msg(TL.COMMAND_DTR_MODIFY_DONE, target.describeTo(context.fPlayer, false), DTRControl.round(target.getDTR()));
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DTR_MODIFY_DESCRIPTION;
    }
}
