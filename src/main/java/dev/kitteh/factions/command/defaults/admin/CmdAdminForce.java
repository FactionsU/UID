package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.admin.force.CmdForceDisband;
import dev.kitteh.factions.command.defaults.admin.force.CmdForceHome;
import dev.kitteh.factions.command.defaults.admin.force.CmdForceKick;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.function.BiConsumer;

public class CmdAdminForce implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> forceBuilder = builder.literal("force");

            new CmdForceKick().consumer().accept(manager, forceBuilder);
            new CmdForceDisband().consumer().accept(manager, forceBuilder);
            new CmdForceHome().consumer().accept(manager, forceBuilder);
        };
    }
}
