package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTDeposit;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTFill;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTSiphon;
import dev.kitteh.factions.command.defaults.tnt.CmdTNTWithdraw;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdTNT implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> tntBuilder = builder.literal("tnt")
                    .commandDescription(Cloudy.desc(TL.COMMAND_TNT_INFO_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasFaction().and(Cloudy.predicate(s -> FactionsPlugin.getInstance().conf().commands().tnt().isEnable()))));

            manager.command(tntBuilder.permission(tntBuilder.commandPermission().and(Cloudy.hasPermission(Permission.TNT_INFO))).handler(this::handle));
            manager.command(tntBuilder.literal("info").permission(tntBuilder.commandPermission().and(Cloudy.hasPermission(Permission.TNT_INFO))).handler(this::handle));

            new CmdTNTDeposit().consumer().accept(manager, tntBuilder);
            new CmdTNTFill().consumer().accept(manager, tntBuilder);
            new CmdTNTSiphon().consumer().accept(manager, tntBuilder);
            new CmdTNTWithdraw().consumer().accept(manager, tntBuilder);
        };
    }

    private void handle(CommandContext<Sender> context) {
        context.sender().msg(TL.COMMAND_TNT_INFO_MESSAGE, ((Sender.Player) context.sender()).faction().tntBank());
    }
}
