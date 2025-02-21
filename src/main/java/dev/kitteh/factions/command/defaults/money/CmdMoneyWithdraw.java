package dev.kitteh.factions.command.defaults.money;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import java.util.function.BiConsumer;

public class CmdMoneyWithdraw implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("withdraw")
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MONEY_WITHDRAW)).and(Cloudy.isPlayer()))
                            .required("amount", DoubleParser.doubleParser(0))
                            .flag(
                                    manager.flagBuilder("faction")
                                            .withComponent(FactionParser.of(FactionParser.Include.SELF))
                                            .withPermission(Cloudy.hasPermission(Permission.MONEY_WITHDRAW_ANY))
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        double amount = Math.abs(context.get("amount"));
        if (amount == 0D) {
            return;
        }

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction;

        if (context.flags().get("faction") instanceof Faction f) {
            faction = f;
        } else {
            faction = sender.getFaction();
        }

        if (!faction.isNormal()) {
            return;
        }

        if (!faction.hasAccess(sender, PermissibleActions.ECONOMY, sender.getLastStoodAt())) {
            sender.msg(TL.GENERIC_NOPERMISSION, PermissibleActions.ECONOMY.getShortDescription());
            return;
        }

        boolean success = Econ.transferMoney(sender, faction, sender, amount);

        if (success && FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
            FactionsPlugin.getInstance().log(ChatColor.stripColor(FactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYWITHDRAW_WITHDRAW.toString(), sender.getName(), Econ.moneyString(amount), faction.describeTo(null))));
        }
    }
}
