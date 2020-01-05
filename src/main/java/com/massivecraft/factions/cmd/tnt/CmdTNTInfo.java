package com.massivecraft.factions.cmd.tnt;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.cmd.FCommand;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdTNTInfo extends FCommand {
    public CmdTNTInfo() {
        super();
        this.aliases.add("info");
        this.aliases.add("status");

        this.requirements = new CommandRequirements.Builder(Permission.TNT_INFO).memberOnly().build();
    }

    @Override
    public void perform(CommandContext context) {
        context.msg(TL.COMMAND_TNT_INFO_MESSAGE, context.faction.getTNTBank());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TNT_INFO_DESCRIPTION;
    }
}
