package dev.kitteh.factions.cmd.money;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.FCommand;

public abstract class MoneyCommand extends FCommand {

    public boolean isEnabled(CommandContext context) {
        if (!super.isEnabled(context)) {
            return false;
        }

        if (!FactionsPlugin.getInstance().conf().economy().isEnabled()) {
            context.msg("<b>Faction economy features are disabled on this server.");
            return false;
        }

        if (!FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            context.msg("<b>The faction bank system is disabled on this server.");
            return false;
        }

        return true;
    }

}
