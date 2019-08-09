package com.massivecraft.factions.cmd.money;

import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.cmd.FCmdRoot;
import com.massivecraft.factions.util.TL;

public class CmdMoney extends MoneyCommand {

    public CmdMoney() {
        super();
        this.aliases.add("money");

        this.helpLong.add(plugin.txt().parseTags(TL.COMMAND_MONEY_LONG.toString()));

        this.addSubCommand(new CmdMoneyBalance());
        this.addSubCommand(new CmdMoneyDeposit());
        this.addSubCommand(new CmdMoneyWithdraw());
        this.addSubCommand(new CmdMoneyTransferFf());
        this.addSubCommand(new CmdMoneyTransferFp());
        this.addSubCommand(new CmdMoneyTransferPf());
    }

    @Override
    public void perform(CommandContext context) {
        context.commandChain.add(this);
        FCmdRoot.getInstance().cmdAutoHelp.execute(context);
    }

    @Override
    public TL getUsageTranslation() {
        return TL.COMMAND_MONEY_DESCRIPTION;
    }

}
