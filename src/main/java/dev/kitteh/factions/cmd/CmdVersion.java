package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;


public class CmdVersion extends FCommand {

    public CmdVersion() {
        this.aliases.add("version");
        this.aliases.add("ver");

        this.requirements = new CommandRequirements.Builder(Permission.VERSION).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        context.msg(TL.COMMAND_VERSION_VERSION, FactionsPlugin.getInstance().getDescription().getFullName());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_VERSION_DESCRIPTION;
    }
}
