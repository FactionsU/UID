package dev.kitteh.factions.command.defaults.money;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import java.util.function.BiConsumer;

public class CmdMoneySend implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> sendBuilder = builder.literal("send")
                    .required("amount", DoubleParser.doubleParser(0))
                    .literal("to");

            manager.command(
                    sendBuilder.literal("faction")
                            .required("faction", FactionParser.of())
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.MONEY_F2F))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.ECONOMY))
                            )
                            .handler(this::handleToFaction)
            );

            manager.command(
                    sendBuilder.literal("player")
                            .required("player", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION, FPlayerParser.Include.OTHER_FACTION))
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.MONEY_F2P))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.ECONOMY))
                            )
                            .handler(this::handleToPlayer)
            );
        };
    }

    private void handleToFaction(CommandContext<Sender> context) {
        double amount = context.get("amount");
        amount = Math.abs(amount);
        if (amount == 0D) {
            return;
        }

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction from = sender.getFaction();

        Faction to = context.get("faction");

        if (!to.isNormal()) {
            return;
        }

        boolean success = Econ.transferMoney(sender, from, to, amount);

        if (success && FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
            FactionsPlugin.getInstance().log(ChatColor.stripColor(AbstractFactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYTRANSFERFF_TRANSFER.toString(), context.sender().sender().getName(), Econ.moneyString(amount), from.describeTo(null), to.describeTo(null))));
        }
    }

    private void handleToPlayer(CommandContext<Sender> context) {
        double amount = Math.abs(context.get("amount"));
        if (amount == 0D) {
            return;
        }

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction from = sender.getFaction();

        FPlayer to = context.get("player");

        boolean success = Econ.transferMoney(sender, from, to, amount);

        if (success && FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
            FactionsPlugin.getInstance().log(ChatColor.stripColor(AbstractFactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYTRANSFERFP_TRANSFER.toString(), sender.getName(), Econ.moneyString(amount), from.describeTo(null), to.describeTo(null))));
        }
    }
}
