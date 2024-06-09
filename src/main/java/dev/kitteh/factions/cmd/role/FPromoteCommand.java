package dev.kitteh.factions.cmd.role;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.perms.PermissibleActions;
import dev.kitteh.factions.perms.Role;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;

public class FPromoteCommand extends FCommand {

    public int relative = 0;

    public FPromoteCommand() {
        super();

        this.requiredArgs.add("player");

        this.requirements = new CommandRequirements.Builder(Permission.PROMOTE)
                .memberOnly()
                .withAction(PermissibleActions.PROMOTE)
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer target = context.argAsBestFPlayerMatch(0);
        if (target == null) {
            // context.msg(TL.GENERIC_NOPLAYERFOUND, context.argAsString(0));
            return;
        }

        if (!target.getFaction().equals(context.faction)) {
            context.msg(TL.COMMAND_PROMOTE_WRONGFACTION, target.getName());
            return;
        }

        Role current = target.getRole();
        Role promotion = Role.getRelative(current, +relative);

        if (promotion == null) {
            context.msg(TL.COMMAND_PROMOTE_NOTTHATPLAYER);
            return;
        }

        // Don't allow people to promote people to their same or higher rnak.
        if (context.fPlayer.getRole().value <= promotion.value) {
            context.msg(TL.COMMAND_PROMOTE_NOT_ALLOWED);
            return;
        }

        if (promotion == Role.COLEADER && !FactionsPlugin.getInstance().conf().factions().other().isAllowMultipleColeaders()) {
            if (!target.getFaction().getFPlayersWhereRole(Role.COLEADER).isEmpty()) {
                context.msg(TL.COMMAND_COLEADER_ALREADY_COLEADER);
                return;
            }
        }

        String action = relative > 0 ? TL.COMMAND_PROMOTE_PROMOTED.toString() : TL.COMMAND_PROMOTE_DEMOTED.toString();

        // Success!
        target.setRole(promotion);
        if (target.isOnline()) {
            target.msg(TL.COMMAND_PROMOTE_TARGET, action, promotion.nicename);
        }

        context.msg(TL.COMMAND_PROMOTE_SUCCESS, action, target.getName(), promotion.nicename);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_PROMOTE_DESCRIPTION;
    }

}
