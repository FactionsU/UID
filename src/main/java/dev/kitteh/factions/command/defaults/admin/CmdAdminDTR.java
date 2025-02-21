package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.admin.dtr.CmdDTRModify;
import dev.kitteh.factions.command.defaults.admin.dtr.CmdDTRResetAll;
import dev.kitteh.factions.command.defaults.admin.dtr.CmdDTRSet;
import dev.kitteh.factions.landraidcontrol.DTRControl;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.function.BiConsumer;

public class CmdAdminDTR implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> dtrBuilder = builder.literal("dtr")
                    .permission(builder.commandPermission().and(Cloudy.predicate(s-> FactionsPlugin.getInstance().getLandRaidControl() instanceof DTRControl)));

            new CmdDTRModify().consumer().accept(manager, dtrBuilder);
            new CmdDTRResetAll().consumer().accept(manager, dtrBuilder);
            new CmdDTRSet().consumer().accept(manager, dtrBuilder);
        };
    }
}
