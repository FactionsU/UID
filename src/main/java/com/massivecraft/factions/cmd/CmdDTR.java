package com.massivecraft.factions.cmd;

import com.massivecraft.factions.cmd.dtr.CmdDTRGet;
import com.massivecraft.factions.cmd.dtr.CmdDTRModify;
import com.massivecraft.factions.cmd.dtr.CmdDTRResetAll;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdDTR extends FCommand {
    private CmdDTRGet cmdDTRGet;

    public CmdDTR() {
        super();
        this.aliases.add("dtr");
        this.aliases.add("deathstilraidable"); // YOLO

        this.addSubCommand(this.cmdDTRGet = new CmdDTRGet());
        this.addSubCommand(new CmdDTRModify());
        this.addSubCommand(new CmdDTRResetAll());

        this.requirements = new CommandRequirements.Builder(Permission.DTR).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        context.commandChain.add(this);
        this.cmdDTRGet.execute(context);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DTR_DESCRIPTION;
    }
}
