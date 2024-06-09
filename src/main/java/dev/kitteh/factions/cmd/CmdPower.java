package dev.kitteh.factions.cmd;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;

public class CmdPower extends FCommand {

    public CmdPower() {
        super();
        this.aliases.add("power");
        this.aliases.add("pow");

        this.optionalArgs.put("player", "you");

        this.requirements = new CommandRequirements.Builder(Permission.POWER).noDisableOnLock().build();
    }

    @Override
    public void perform(CommandContext context) {
        FPlayer target = context.argAsBestFPlayerMatch(0, context.fPlayer);
        if (target == null) {
            return;
        }

        if (target != context.fPlayer && !Permission.POWER_ANY.has(context.sender, true)) {
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make 'em pay
        if (!context.payForCommand(FactionsPlugin.getInstance().conf().economy().getCostPower(), TL.COMMAND_POWER_TOSHOW, TL.COMMAND_POWER_FORSHOW)) {
            return;
        }

        double powerBoost = target.getPowerBoost();
        String boost = (powerBoost == 0.0) ? "" : (powerBoost > 0.0 ? TL.COMMAND_POWER_BONUS.toString() : TL.COMMAND_POWER_PENALTY.toString()) + powerBoost + ")";
        context.msg(TL.COMMAND_POWER_POWER, target.describeTo(context.fPlayer, true), target.getPowerRounded(), target.getPowerMaxRounded(), boost);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_POWER_DESCRIPTION;
    }

}
