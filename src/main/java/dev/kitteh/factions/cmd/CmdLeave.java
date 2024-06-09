package dev.kitteh.factions.cmd;

import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdLeave extends FCommand {

    public CmdLeave() {
        super();
        this.aliases.add("leave");

        this.requirements = new CommandRequirements.Builder(Permission.LEAVE)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        context.fPlayer.leave(true);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.LEAVE_DESCRIPTION;
    }

}
