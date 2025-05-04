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
                    .permission(builder.commandPermission().and(Cloudy.predicate(s->FactionsPlugin.getInstance().conf().economy().isBankEnabled()).and(Cloudy.hasPermission(Permission.MONEY_MODIFY))))
                    .required("faction", FactionParser.of(FactionParser.Include.SELF));


            manager.command(
                    moneyBuilder.literal("modify")
                            .required("amount", DoubleParser.doubleParser())
                            .flag(manager.flagBuilder("notify"))
                            .handler(ctx-> this.handle(ctx, true))
            );

            manager.command(
                    moneyBuilder.literal("set")
                            .required("amount", DoubleParser.doubleParser())
                            .flag(manager.flagBuilder("notify"))
                            .handler(ctx-> this.handle(ctx, false))
            );
        };
    }

    private void handle(CommandContext<Sender> context, boolean modify) {
        if (!FactionsPlugin.getInstance().conf().economy().isBankEnabled()) {
            context.sender().msg(TL.ECON_DISABLED);
            return;
        }
        double amount = context.get("amount");
        Participator faction = context.get("faction");
        boolean notify = context.flags().contains("notify");

        if (Econ.modifyBalance(faction, amount)) {
            context.sender().msg(TL.COMMAND_MONEYMODIFY_MODIFIED, faction.describeTo(context.sender().fPlayerOrNull()), Econ.moneyString(amount));
            if (notify) {
                faction.msg(AbstractFactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYMODIFY_NOTIFY.toString(), faction.describeTo(null), Econ.moneyString(amount)));
            }

            if (FactionsPlugin.getInstance().conf().logging().isMoneyTransactions()) {
                FactionsPlugin.getInstance().log(ChatColor.stripColor(AbstractFactionsPlugin.getInstance().txt().parse(TL.COMMAND_MONEYMODIFY_MODIFIED.toString(), faction.describeTo(null), Econ.moneyString(amount))));
            }
        }
    }
}
