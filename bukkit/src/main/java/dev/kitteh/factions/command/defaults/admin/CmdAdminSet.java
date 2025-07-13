package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.admin.set.*;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdAdminSet implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> setBuilder = builder.literal("set");

            new CmdSetAutoSave().consumer().accept(manager, setBuilder, help);
            new CmdSetGrace().consumer().accept(manager, setBuilder, help);
            new CmdSetPeaceful().consumer().accept(manager, setBuilder, help);
            new CmdSetPermanent().consumer().accept(manager, setBuilder, help);
            new CmdSetTag().consumer().accept(manager, setBuilder, help);
            new CmdSetMaxVaults().consumer().accept(manager, setBuilder, help);
        };
    }
}
