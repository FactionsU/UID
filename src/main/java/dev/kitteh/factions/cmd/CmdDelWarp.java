package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.perms.PermissibleActions;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;

public class CmdDelWarp extends FCommand {

    public CmdDelWarp() {
        super();
        this.aliases.add("delwarp");
        this.aliases.add("dw");
        this.aliases.add("deletewarp");

        this.requiredArgs.add("warp");

        this.requirements = new CommandRequirements.Builder(Permission.SETWARP)
                .memberOnly()
                .withAction(PermissibleActions.SETWARP)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        String warp = context.argAsString(0);
        if (context.faction.isWarp(warp)) {
            if (!transact(context.fPlayer, context)) {
                return;
            }
            context.faction.removeWarp(warp);
            context.msg(TL.COMMAND_DELFWARP_DELETED, warp);
        } else {
            context.msg(TL.COMMAND_DELFWARP_INVALID, warp);
        }
    }

    private boolean transact(FPlayer player, CommandContext context) {
        return player.isAdminBypassing() || context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostDelWarp(), TL.COMMAND_DELFWARP_TODELETE.toString(), TL.COMMAND_DELFWARP_FORDELETE.toString());
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_DELFWARP_DESCRIPTION;
    }
}
