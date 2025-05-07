package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.admin.set.*;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.function.BiConsumer;

public class CmdAdminSet implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> setBuilder = builder.literal("set");

            new CmdSetAutoSave().consumer().accept(manager, setBuilder);
            new CmdSetGrace().consumer().accept(manager, setBuilder);
            new CmdSetPeaceful().consumer().accept(manager, setBuilder);
            new CmdSetPermanent().consumer().accept(manager, setBuilder);
            new CmdSetTag().consumer().accept(manager, setBuilder);

            new CmdSetMaxVaults().consumer().accept(manager, setBuilder);
        };
    }
}
