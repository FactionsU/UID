package dev.kitteh.factions.cmd.money;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.iface.EconomyParticipator;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;


public class CmdMoneyTransferFf extends MoneyCommand {

    public CmdMoneyTransferFf() {
        this.aliases.add("ff");

        this.requiredArgs.add("amount");
        this.requiredArgs.add("faction");
        this.requiredArgs.add("faction");

        this.requirements = new CommandRequirements.Builder(Permission.MONEY_F2F).build();
    }

    @Override
    public void perform(CommandContext context) {
        double amount = Math.abs(context.argAsDouble(0, 0d));
        EconomyParticipator from = context.argAsFaction(1);
        if (from == null) {
            return;
        }
        EconomyParticipator to = context.argAsFaction(2);
        if (to == null) {
            return;
        }

        boolean success = Econ.transferMoney(context.fPlayer, from, to, amount);

        if (success && FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
            String name = context.sender instanceof Player ? context.fPlayer.getName() : context.sender.getName();
            FactionsPlugin.getInstance().log(ChatColor.stripColor(FactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYTRANSFERFF_TRANSFER.toString(), name, Econ.moneyString(amount), from.describeTo(null), to.describeTo(null))));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MONEYTRANSFERFF_DESCRIPTION;
    }
}
