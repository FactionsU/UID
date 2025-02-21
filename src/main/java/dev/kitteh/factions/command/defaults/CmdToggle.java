package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.toggle.CmdToggleChat;
import dev.kitteh.factions.command.defaults.toggle.CmdToggleLogins;
import dev.kitteh.factions.command.defaults.toggle.CmdToggleScoreboard;
import dev.kitteh.factions.command.defaults.toggle.CmdToggleSeeChunk;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.function.BiConsumer;

public class CmdToggle implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> toggleBuilder = builder.literal("toggle").permission(builder.commandPermission().and(Cloudy.hasFaction()));
            new CmdToggleChat().consumer().accept(manager, toggleBuilder);
            new CmdToggleSeeChunk().consumer().accept(manager, toggleBuilder);
            new CmdToggleLogins().consumer().accept(manager, toggleBuilder);
            new CmdToggleScoreboard().consumer().accept(manager, toggleBuilder);
        };
    }
}
