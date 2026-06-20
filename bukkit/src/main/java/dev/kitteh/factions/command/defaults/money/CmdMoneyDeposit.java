package dev.kitteh.factions.command.defaults.money;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.DoubleParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdMoneyDeposit implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = FactionsPlugin.instance().tl().commands().money().deposit();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MONEY_DEPOSIT)).and(Cloudy.isPlayer()))
                            .required("amount", DoubleParser.doubleParser(0))
                            .flag(manager.flagBuilder("faction").withComponent(FactionParser.of(FactionParser.Include.SELF)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        double amount = context.get("amount");
        amount = Math.abs(amount);
        if (amount == 0D) {
            return;
        }

        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction;

        if (context.flags().get("faction") instanceof Faction f) {
            faction = f;
        } else {
            faction = sender.faction();
        }

        if (!faction.isNormal()) {
            return;
        }

        boolean success = Econ.transferMoney(sender, sender, faction, amount);

        if (success && FactionsPlugin.instance().conf().logging().isMoneyTransactions()) {
            AbstractFactionsPlugin.instance().log(String.format("%s deposited %s in the faction bank: %s", sender.name(), Econ.moneyString(amount), faction.tag()));
        }
    }
}
