package dev.kitteh.factions.cmd.money;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;


public class CmdMoneyModify extends MoneyCommand {

    public CmdMoneyModify() {
        this.aliases.add("modify");

        this.requiredArgs.add("amount");
        this.requiredArgs.add("faction");
        this.optionalArgs.put("notify", "true/false");

        this.requirements = new CommandRequirements.Builder(Permission.MONEY_MODIFY).build();
    }

    @Override
    public void perform(CommandContext context) {
        if (!FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            context.sendMessage(TL.ECON_DISABLED.toString());
            return;
        }
        double amount = context.argAsDouble(0, 0d);
        Participator faction = context.argAsFaction(1);
        if (faction == null) {
            return;
        }
        boolean notify = context.argAsBool(2, false);

        if (Econ.modifyBalance(faction, amount)) {
            String success = FactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYMODIFY_MODIFIED.toString(), faction.describeTo(null), Econ.moneyString(amount));

            context.sendMessage(success);
            if (notify) {
                faction.msg(FactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYMODIFY_NOTIFY.toString(), faction.describeTo(null), Econ.moneyString(amount)));
            }

            if (FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
                FactionsPlugin.getInstance().log(ChatColor.stripColor(success));
            }
        }

    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MONEYMODIFY_DESCRIPTION;
    }
}
