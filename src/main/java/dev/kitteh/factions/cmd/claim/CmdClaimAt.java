package dev.kitteh.factions.cmd.claim;

import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.cmd.FCommand;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdClaimAt extends FCommand {

    public CmdClaimAt() {
        super();
        this.aliases.add("claimat");

        this.requiredArgs.add("world");
        this.requiredArgs.add("x");
        this.requiredArgs.add("z");

        this.requirements = new CommandRequirements.Builder(Permission.CLAIMAT)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        int x = context.argAsInt(1);
        int z = context.argAsInt(2);
        FLocation location = new FLocation(context.argAsString(0), x, z);
        context.fPlayer.attemptClaim(context.faction, location, true);
    }

    @Override
    public TL getUsageTranslation() {
        return null;
    }
}
