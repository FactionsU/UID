package dev.kitteh.factions.cmd;

import dev.kitteh.factions.cmd.dtr.CmdDTRGet;
import dev.kitteh.factions.cmd.dtr.CmdDTRModify;
import dev.kitteh.factions.cmd.dtr.CmdDTRResetAll;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;

public class CmdDTR extends FCommand {
    private final CmdDTRGet cmdDTRGet;

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
