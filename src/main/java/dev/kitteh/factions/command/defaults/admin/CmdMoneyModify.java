package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.Participator;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import java.util.function.BiConsumer;

public class CmdMoneyModify implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> moneyBuilder = builder.literal("money")
                    .commandDescription(Cloudy.desc(TL.COMMAND_MONEYMODIFY_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().economy().isBankEnabled()).and(Cloudy.hasPermission(Permission.MONEY_MODIFY))))
                    .required("faction", FactionParser.of(FactionParser.Include.SELF));


            manager.command(
                    moneyBuilder.literal("modify")
                            .required("amount", DoubleParser.doubleParser())
                            .flag(manager.flagBuilder("notify"))
                            .handler(ctx -> this.handle(ctx, true))
            );

            manager.command(
                    moneyBuilder.literal("set")
                            .required("amount", DoubleParser.doubleParser())
                            .flag(manager.flagBuilder("notify"))
                            .handler(ctx -> this.handle(ctx, false))
            );
        };
    }

    private void handle(CommandContext<Sender> context, boolean modify) {
        if (!FactionsPlugin.instance().conf().economy().isBankEnabled()) {
            context.sender().msg(TL.ECON_DISABLED);
            return;
        }
        double amount = context.get("amount");
        Participator faction = context.get("faction");
        boolean notify = context.flags().contains("notify");


        if (modify) {
            if (Econ.modifyBalance(faction, amount)) {
                context.sender().msg(TL.COMMAND_MONEYMODIFY_MODIFIED, faction.describeToLegacy(context.sender().fPlayerOrNull()), Econ.moneyString(amount));
                if (notify) {
                    faction.msgLegacy(TL.COMMAND_MONEYMODIFY_NOTIFY, faction.describeToLegacy(null), Econ.moneyString(amount));
                }

                if (FactionsPlugin.instance().conf().logging().isMoneyTransactions()) {
                    AbstractFactionsPlugin.instance().log(ChatColor.stripColor(TextUtil.parse(TL.COMMAND_MONEYMODIFY_MODIFIED.toString(), faction.describeToLegacy(null), Econ.moneyString(amount))));
                }
            } else {
                context.sender().msg(TL.COMMAND_MONEYMODIFY_FAIL);
            }
        } else {
            if (Econ.setBalance(faction, amount)) {
                context.sender().msg(TL.COMMAND_MONEYMODIFY_SET, faction.describeToLegacy(context.sender().fPlayerOrNull()), Econ.moneyString(amount));
                if (notify) {
                    faction.msgLegacy(TL.COMMAND_MONEYMODIFY_SETNOTIFY, faction.describeToLegacy(null), Econ.moneyString(amount));
                }

                if (FactionsPlugin.instance().conf().logging().isMoneyTransactions()) {
                    AbstractFactionsPlugin.instance().log(ChatColor.stripColor(TextUtil.parse(TL.COMMAND_MONEYMODIFY_SET.toString(), faction.describeToLegacy(null), Econ.moneyString(amount))));
                }
            } else {
                context.sender().msg(TL.COMMAND_MONEYMODIFY_FAIL);
            }
        }
    }
}
