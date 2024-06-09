package dev.kitteh.factions.cmd.dtr;

import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;

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
