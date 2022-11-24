package com.massivecraft.factions.cmd.money;

import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.CommandRequirements;
import com.massivecraft.factions.iface.EconomyParticipator;
import com.massivecraft.factions.integration.Econ;
import com.massivecraft.factions.struct.Permission;
import com.massivecraft.factions.util.TL;
import org.bukkit.ChatColor;


public class CmdMoneyModify extends MoneyCommand {

    public CmdMoneyModify() {
        this.aliases.add("modify");

        this.requiredArgs.add("amount");
        this.requiredArgs.add("faction");

        this.requirements = new CommandRequirements.Builder(Permission.MONEY_MODIFY).build();
    }

    @Override
    public void perform(CommandContext context) {
        if (!FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            context.sendMessage(TL.ECON_DISABLED.toString());
            return;
        }
        double amount = context.argAsDouble(0, 0d);
        EconomyParticipator faction = context.argAsFaction(1);
        if (faction == null) {
            return;
        }

        if (Econ.modifyBalance(faction, amount)) {
            String success = FactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYMODIFY_MODIFIED.toString(), faction.describeTo(null), Econ.moneyString(amount));

            context.sendMessage(success);

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
