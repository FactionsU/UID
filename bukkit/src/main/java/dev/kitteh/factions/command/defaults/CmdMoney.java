package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.money.CmdMoneyBalance;
import dev.kitteh.factions.command.defaults.money.CmdMoneyDeposit;
import dev.kitteh.factions.command.defaults.money.CmdMoneySend;
import dev.kitteh.factions.command.defaults.money.CmdMoneyWithdraw;
import dev.kitteh.factions.integration.Econ;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdMoney implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> moneyBuilder = builder.literal("money")
                    .permission(builder.commandPermission().and(Cloudy.predicate(s -> Econ.shouldBeUsedWithBanks())));

            new CmdMoneyBalance().consumer().accept(manager, moneyBuilder, help);
            new CmdMoneyDeposit().consumer().accept(manager, moneyBuilder, help);
            new CmdMoneySend().consumer().accept(manager, moneyBuilder, help);
            new CmdMoneyWithdraw().consumer().accept(manager, moneyBuilder, help);

            manager.command(moneyBuilder.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f money *", ctx.sender())));
        };
    }
}
