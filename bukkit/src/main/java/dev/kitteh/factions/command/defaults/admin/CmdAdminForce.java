package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.admin.force.CmdForceDisband;
import dev.kitteh.factions.command.defaults.admin.force.CmdForceHome;
import dev.kitteh.factions.command.defaults.admin.force.CmdForceKick;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdAdminForce implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> forceBuilder = builder.literal("force");

            new CmdForceKick().consumer().accept(manager, forceBuilder, help);
            new CmdForceDisband().consumer().accept(manager, forceBuilder, help);
            new CmdForceHome().consumer().accept(manager, forceBuilder, help);
        };
    }
}
