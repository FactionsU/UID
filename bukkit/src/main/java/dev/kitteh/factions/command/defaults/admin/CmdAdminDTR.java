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

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdAdminDTR implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> dtrBuilder = builder.literal("dtr")
                    .permission(builder.commandPermission().and(Cloudy.predicate(s -> FactionsPlugin.instance().landRaidControl() instanceof DTRControl)));

            new CmdDTRModify().consumer().accept(manager, dtrBuilder, help);
            new CmdDTRResetAll().consumer().accept(manager, dtrBuilder, help);
            new CmdDTRSet().consumer().accept(manager, dtrBuilder, help);
        };
    }
}
