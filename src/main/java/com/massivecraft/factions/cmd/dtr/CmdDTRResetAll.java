package com.massivecraft.factions.cmd.dtr;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.landraidcontrol.DTRControl;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdDTRResetAll extends FCommand {

    public CmdDTRResetAll() {
        super();
        this.aliases.add("resetall");

        this.requirements = new CommandRequirements.Builder(Permission.MODIFY_DTR).build();
    }

    @Override
    public void perform(CommandContext context) {
        if (context.fPlayer != null) {
            return;
        }

        DTRControl dtr = (DTRControl) FactionsPlugin.getInstance().getLandRaidControl();
        Factions.getInstance().getAllFactions().forEach(target -> target.setDTR(dtr.getMaxDTR(target)));
        context.msg(TL.COMMAND_DTR_MODIFY_DONE, "EVERYONE", "MAX");
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DTR_MODIFY_DESCRIPTION;
    }
}
