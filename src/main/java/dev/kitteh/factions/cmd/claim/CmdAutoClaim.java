package dev.kitteh.factions.cmd.claim;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdAutoClaim extends FCommand {

    public CmdAutoClaim() {
        super();
        this.aliases.add("autoclaim");

        //this.requiredArgs.add("");
        this.optionalArgs.put("faction", "your");

        this.requirements = new CommandRequirements.Builder(Permission.AUTOCLAIM)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        Faction forFaction = context.argAsFaction(0, context.faction);
        if (forFaction == null || forFaction == context.fPlayer.getAutoClaimFor()) {
            context.fPlayer.setAutoClaimFor(null);
            context.msg(TL.COMMAND_AUTOCLAIM_DISABLED);
            return;
        }

        if (!context.fPlayer.canClaimForFaction(forFaction)) {
            if (context.faction == forFaction) {
                context.msg(TL.CLAIM_CANTCLAIM, forFaction.describeTo(context.fPlayer));
            } else {
                context.msg(TL.COMMAND_AUTOCLAIM_OTHERFACTION, forFaction.describeTo(context.fPlayer));
            }

            return;
        }

        context.fPlayer.setAutoClaimFor(forFaction);

        context.msg(TL.COMMAND_AUTOCLAIM_ENABLED, forFaction.describeTo(context.fPlayer));
        context.fPlayer.attemptClaim(forFaction, context.player.getLocation(), true);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOCLAIM_DESCRIPTION;
    }

}
