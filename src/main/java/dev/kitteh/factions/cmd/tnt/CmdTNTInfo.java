package dev.kitteh.factions.cmd.tnt;

import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

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
