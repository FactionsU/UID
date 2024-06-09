package dev.kitteh.factions.cmd.claim;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.SpiralTask;
import dev.kitteh.factions.util.TL;

public class CmdUnclaim extends FCommand {

    public CmdUnclaim() {
        this.aliases.add("unclaim");
        this.aliases.add("declaim");

        this.optionalArgs.put("radius", "1");
        this.optionalArgs.put("faction", "your");

        this.requirements = new CommandRequirements.Builder(Permission.UNCLAIM)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(final CommandContext context) {
        // Read and validate input
        int radius = context.argAsInt(0, 1); // Default to 1
        final Faction forFaction = context.argAsFaction(1, context.faction); // Default to own

        if (radius < 1) {
            context.msg(TL.COMMAND_CLAIM_INVALIDRADIUS);
            return;
        }

        if (radius < 2) {
            // single chunk
            context.fPlayer.attemptUnclaim(forFaction, new FLocation(context.player), true);
        } else {
            // radius claim
            if (!Permission.CLAIM_RADIUS.has(context.sender, false)) {
                context.msg(TL.COMMAND_CLAIM_DENIED);
                return;
            }

            new SpiralTask(new FLocation(context.player), radius) {
                private int failCount = 0;
                private final int limit = FactionsPlugin.getInstance().conf().factions().claims().getRadiusClaimFailureLimit() - 1;

                @Override
                public boolean work() {
                    boolean success = context.fPlayer.attemptUnclaim(forFaction, this.currentFLocation(), true);
                    if (success) {
                        failCount = 0;
                    } else if (failCount++ >= limit) {
                        this.stop();
                        return false;
                    }

                    return true;
                }
            };
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_UNCLAIM_DESCRIPTION;
    }

}
