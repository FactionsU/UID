package dev.kitteh.factions.cmd;

import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdSeeChunk extends FCommand {
    public CmdSeeChunk() {
        super();
        this.aliases.add("seechunk");
        this.aliases.add("sc");

        this.requirements = new CommandRequirements.Builder(Permission.SEECHUNK)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        boolean toggle = context.args.isEmpty() ? !context.fPlayer.isSeeingChunk() : context.argAsBool(0);
        context.fPlayer.setSeeingChunk(toggle);
        context.msg(TL.COMMAND_SEECHUNK_TOGGLE, toggle ? "enabled" : "disabled");
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SEECHUNK_DESCRIPTION;
    }
}
