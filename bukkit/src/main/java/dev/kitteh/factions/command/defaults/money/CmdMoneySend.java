package dev.kitteh.factions.command.defaults.money;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.DoubleParser;

public class CmdMoneySend implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().money().send();

            Command.Builder<Sender> sendBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .required("amount", DoubleParser.doubleParser(0))
                    .literal(tl.getSubCmdTo());

            manager.command(
                    sendBuilder.literal(tl.getSubCmdFaction())
                            .required("faction", FactionParser.of())
                            .permission(
                                    builder.commandPermission()
                                            .and(Cloudy.hasPermission(Permission.MONEY_F2F))
                                            .and(Cloudy.hasSelfFactionPerms(PermissibleActions.ECONOMY))
                            )
                            .handler(this::handleToFaction)
            );

            manager.command(
                    sendBuilder.literal(tl.getSubCmdPlayer())
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
        Faction from = sender.faction();

        Faction to = context.get("faction");

        if (!to.isNormal()) {
            return;
        }

        boolean success = Econ.transferMoney(sender, from, to, amount);

        if (success && Confs.main().logging().isMoneyTransactions()) {
            AbstractFactionsPlugin.instance().log(String.format("%s transferred %s from the faction \"%s\" to the faction \"%s\"", context.sender().sender().getName(), Econ.moneyString(amount), from.tag(), to.tag()));
        }
    }

    private void handleToPlayer(CommandContext<Sender> context) {
        double amount = context.get("amount");
        amount = Math.abs(amount);
        if (amount == 0D) {
            return;
        }

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction from = sender.faction();

        FPlayer to = context.get("player");

        boolean success = Econ.transferMoney(sender, from, to, amount);

        if (success && Confs.main().logging().isMoneyTransactions()) {
            AbstractFactionsPlugin.instance().log(String.format("%s transferred %s from the faction \"%s\" to the player \"%s\"", sender.name(), Econ.moneyString(amount), from.tag(), to.name()));
        }
    }
}
