package dev.kitteh.factions.cmd;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;


public class CmdOwnerList extends FCommand {

    public CmdOwnerList() {
        super();
        this.aliases.add("ownerlist");

        this.requirements = new CommandRequirements.Builder(Permission.OWNERLIST)
                .playerOnly()
                .noDisableOnLock()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        boolean hasBypass = context.fPlayer.isAdminBypassing();

        if (!hasBypass && !context.assertHasFaction()) {
            return;
        }

        if (!FactionsPlugin.getInstance().conf().factions().ownedArea().isEnabled()) {
            context.msg(TL.COMMAND_OWNERLIST_DISABLED);
            return;
        }

        FLocation flocation = new FLocation(context.player);

        Faction faction = context.faction;

        if (Board.getInstance().getFactionAt(flocation) != context.faction) {
            if (!hasBypass) {
                context.msg(TL.COMMAND_OWNERLIST_WRONGFACTION);
                return;
            }
            //TODO: This code won't ever be called.
            faction = Board.getInstance().getFactionAt(flocation);
            if (!faction.isNormal()) {
                context.msg(TL.COMMAND_OWNERLIST_NOTCLAIMED);
                return;
            }
        }

        String owners = faction.getOwnerListString(flocation);

        if (owners == null || owners.isEmpty()) {
            context.msg(TL.COMMAND_OWNERLIST_NONE);
            return;
        }

        context.msg(TL.COMMAND_OWNERLIST_OWNERS, owners);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_OWNERLIST_DESCRIPTION;
    }
}
