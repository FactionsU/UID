package com.massivecraft.factions.cmd.dtr;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.landraidcontrol.DTRControl;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

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
