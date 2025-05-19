package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
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

import java.util.function.BiConsumer;

public class CmdMoney implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> moneyBuilder = builder.literal("money")
                    .permission(builder.commandPermission().and(Cloudy.predicate(s -> Econ.shouldBeUsed() && FactionsPlugin.instance().conf().economy().isBankEnabled())));

            new CmdMoneyBalance().consumer().accept(manager, moneyBuilder);
            new CmdMoneyDeposit().consumer().accept(manager, moneyBuilder);
            new CmdMoneySend().consumer().accept(manager, moneyBuilder);
            new CmdMoneyWithdraw().consumer().accept(manager, moneyBuilder);
        };
    }
}
