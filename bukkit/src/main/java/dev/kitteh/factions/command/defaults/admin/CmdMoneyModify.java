package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdMoneyModify implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().money();

            Command.Builder<Sender> moneyBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.predicate(s -> FactionsPlugin.instance().conf().economy().isBankEnabled()).and(Cloudy.hasPermission(Permission.MONEY_MODIFY))))
                    .required("faction", FactionParser.of(FactionParser.Include.SELF));


            manager.command(
                    moneyBuilder.literal(tl.getSubCmdModify())
                            .required("amount", DoubleParser.doubleParser())
                            .flag(manager.flagBuilder("notify"))
                            .handler(ctx -> this.handle(ctx, true))
            );

            manager.command(
                    moneyBuilder.literal(tl.getSubCmdSet())
                            .required("amount", DoubleParser.doubleParser())
                            .flag(manager.flagBuilder("notify"))
                            .handler(ctx -> this.handle(ctx, false))
            );
        };
    }

    private void handle(CommandContext<Sender> context, boolean modify) {
        var tl = FactionsPlugin.instance().tl().commands().admin().money();
        Sender sender = context.sender();

        double amount = context.get("amount");
        Faction faction = context.get("faction");
        boolean notify = context.flags().contains("notify");


        if (modify) {
            if (Econ.modifyBalance(faction, amount)) {
                sender.sendRichMessage(tl.getModified(), FactionResolver.of(sender.fPlayerOrNull(), faction), Placeholder.unparsed("amount", Econ.moneyString(amount)));
                if (notify) {
                    sender.sendRichMessage(tl.getModifyNotify(), FactionResolver.of((Player) null, faction), Placeholder.unparsed("amount", Econ.moneyString(amount)));
                }

                if (FactionsPlugin.instance().conf().logging().isMoneyTransactions()) {
                    AbstractFactionsPlugin.instance().log(faction.tag() + " bank modified by " + Econ.moneyString(amount));
                }
            } else {
                sender.sendRichMessage(tl.getFail(), FactionResolver.of(sender.fPlayerOrNull(), faction), Placeholder.unparsed("amount", Econ.moneyString(amount)));
            }
        } else {
            if (Econ.setBalance(faction, amount)) {
                sender.sendRichMessage(tl.getSet(), FactionResolver.of(sender.fPlayerOrNull(), faction), Placeholder.unparsed("amount", Econ.moneyString(amount)));
                if (notify) {
                    sender.sendRichMessage(tl.getSetNotify(), FactionResolver.of((Player) null, faction), Placeholder.unparsed("amount", Econ.moneyString(amount)));
                }

                if (FactionsPlugin.instance().conf().logging().isMoneyTransactions()) {
                    AbstractFactionsPlugin.instance().log(faction.tag() + " bank set to " + Econ.moneyString(amount));
                }
            } else {
                sender.sendRichMessage(tl.getFail(), FactionResolver.of(sender.fPlayerOrNull(), faction), Placeholder.unparsed("amount", Econ.moneyString(amount)));
            }
        }
    }
}
