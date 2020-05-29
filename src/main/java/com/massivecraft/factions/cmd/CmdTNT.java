package com.massivecraft.factions.cmd;

import com.massivecraft.factions.cmd.tnt.CmdTNTDeposit;
import com.massivecraft.factions.cmd.tnt.CmdTNTFill;
import com.massivecraft.factions.cmd.tnt.CmdTNTInfo;
import com.massivecraft.factions.cmd.tnt.CmdTNTSiphon;
import com.massivecraft.factions.cmd.tnt.CmdTNTWithdraw;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;

public class CmdTNT extends FCommand {
    private final CmdTNTInfo infoCmd;

    public CmdTNT() {
        super();
        this.aliases.add("tnt");
        this.aliases.add("trinitrotoluene");

        this.addSubCommand(this.infoCmd = new CmdTNTInfo());
        this.addSubCommand(new CmdTNTFill());
        this.addSubCommand(new CmdTNTDeposit());
        this.addSubCommand(new CmdTNTWithdraw());
        this.addSubCommand(new CmdTNTSiphon());

        this.requirements = new CommandRequirements.Builder(Permission.TNT_INFO).memberOnly().build();
    }

    @Override
    public void perform(CommandContext context) {
        context.commandChain.add(this);
        this.infoCmd.execute(context);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_TNT_INFO_DESCRIPTION;
    }
}
