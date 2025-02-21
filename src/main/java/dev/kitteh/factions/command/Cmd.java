package dev.kitteh.factions.command;

import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.function.BiConsumer;

public interface Cmd {
    BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer();
}
