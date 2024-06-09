package dev.kitteh.factions.cmd.money;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.iface.EconomyParticipator;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.struct.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;


public class CmdMoneyDeposit extends MoneyCommand {

    public CmdMoneyDeposit() {
        super();
        this.aliases.add("d");
        this.aliases.add("deposit");

        this.requiredArgs.add("amount");
        this.optionalArgs.put("faction", "yours");

        this.requirements = new CommandRequirements.Builder(Permission.MONEY_DEPOSIT)
                .memberOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        double amount = Math.abs(context.argAsDouble(0, 0d));
        EconomyParticipator faction = context.argAsFaction(1, context.faction);
        if (faction == null) {
            return;
        }
        boolean success = Econ.transferMoney(context.fPlayer, context.fPlayer, faction, amount);

        if (success && FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
            FactionsPlugin.getInstance().log(ChatColor.stripColor(FactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYDEPOSIT_DEPOSITED.toString(), context.fPlayer.getName(), Econ.moneyString(amount), faction.describeTo(null))));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MONEYDEPOSIT_DESCRIPTION;
    }

}
