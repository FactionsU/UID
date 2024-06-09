package dev.kitteh.factions.cmd;

import dev.kitteh.factions.cmd.tnt.CmdTNTDeposit;
import dev.kitteh.factions.cmd.tnt.CmdTNTFill;
import dev.kitteh.factions.cmd.tnt.CmdTNTInfo;
import dev.kitteh.factions.cmd.tnt.CmdTNTSiphon;
import dev.kitteh.factions.cmd.tnt.CmdTNTWithdraw;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;

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
