package dev.kitteh.factions.cmd;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;

public class CmdSaveAll extends FCommand {

    public CmdSaveAll() {
        super();
        this.aliases.add("saveall");
        this.aliases.add("save");

        this.requirements = new CommandRequirements.Builder(Permission.SAVE).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayers.getInstance().forceSave(false);
        Factions.getInstance().forceSave(false);
        Board.getInstance().forceSave(false);
        context.msg(TL.COMMAND_SAVEALL_SUCCESS);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_SAVEALL_DESCRIPTION;
    }

}