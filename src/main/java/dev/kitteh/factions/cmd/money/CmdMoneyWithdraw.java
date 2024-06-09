package dev.kitteh.factions.cmd.money;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.cmd.CommandContext;
import dev.kitteh.factions.cmd.CommandRequirements;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;


public class CmdMoneyWithdraw extends MoneyCommand {

    public CmdMoneyWithdraw() {
        this.aliases.add("w");
        this.aliases.add("withdraw");

        this.requiredArgs.add("amount");
        this.optionalArgs.put("faction", "yours");

        this.requirements = new CommandRequirements.Builder(Permission.MONEY_F2P)
                .playerOnly()
                .build();
    }

    @Override
    public void perform(CommandContext context) {
        double amount = Math.abs(context.argAsDouble(0, 0d));
        Participator faction = context.argAsFaction(1, context.faction);
        if (faction == null) {
            return;
        }

        if (!context.faction.hasAccess(context.fPlayer, PermissibleActions.ECONOMY, context.fPlayer.getLastStoodAt())) {
            context.msg(TL.GENERIC_NOPERMISSION, PermissibleActions.ECONOMY.getShortDescription());
            return;
        }

        boolean success = Econ.transferMoney(context.fPlayer, faction, context.fPlayer, amount);

        if (success && FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
            FactionsPlugin.getInstance().log(ChatColor.stripColor(FactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYWITHDRAW_WITHDRAW.toString(), context.fPlayer.getName(), Econ.moneyString(amount), faction.describeTo(null))));
        }
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MONEYWITHDRAW_DESCRIPTION;
    }
}
