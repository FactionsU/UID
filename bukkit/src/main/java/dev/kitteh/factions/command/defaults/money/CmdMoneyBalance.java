package dev.kitteh.factions.command.defaults.money;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdMoneyBalance implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().money().balance();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.MONEY_BALANCE)))
                            .flag(
                                    manager.flagBuilder("faction")
                                            .withComponent(FactionParser.of(FactionParser.Include.SELF))
                                            .withPermission(Cloudy.hasPermission(Permission.MONEY_BALANCE_ANY))
                            )
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer fPlayer = context.sender().fPlayerOrNull();

        Faction faction = null;

        if (context.flags().get("faction") instanceof Faction f) {
            faction = f;
        }
        if (faction == null && fPlayer != null) {
            faction = fPlayer.faction();
        }

        var tl = Confs.tl().commands().money();
        if (faction == null || !faction.isNormal()) {
            context.sender().sendRichMessage(tl.getNoFaction());
            return;
        }

        if (fPlayer != null) {
            Econ.sendBalanceInfo(fPlayer, faction);
        } else {
            Econ.sendBalanceInfo(context.sender().sender(), faction);
        }
    }
}
