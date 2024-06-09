package dev.kitteh.factions.cmd.claim;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;

public class CmdAutoUnclaim extends FCommand {

    public CmdAutoUnclaim() {
        super();
        this.aliases.add("autounclaim");

        //this.requiredArgs.add("");
        this.optionalArgs.put("faction", "your");

        this.requirements = new CommandRequirements.Builder(Permission.AUTOCLAIM)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        Faction forFaction = context.argAsFaction(0, context.faction);
        if (forFaction == null || forFaction == context.fPlayer.getAutoUnclaimFor()) {
            context.fPlayer.setAutoUnclaimFor(null);
            context.msg(TL.COMMAND_AUTOUNCLAIM_DISABLED);
            return;
        }

        if (!context.fPlayer.canClaimForFaction(forFaction)) {
            if (context.faction == forFaction) {
                context.msg(TL.CLAIM_CANTUNCLAIM, forFaction.describeTo(context.fPlayer));
            } else {
                context.msg(TL.COMMAND_AUTOUNCLAIM_OTHERFACTION, forFaction.describeTo(context.fPlayer));
            }

            return;
        }

        context.fPlayer.setAutoUnclaimFor(forFaction);

        context.msg(TL.COMMAND_AUTOUNCLAIM_ENABLED, forFaction.describeTo(context.fPlayer));
        context.fPlayer.attemptUnclaim(forFaction, new FLocation(context.player.getLocation()), true);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_AUTOUNCLAIM_DESCRIPTION;
    }

}
