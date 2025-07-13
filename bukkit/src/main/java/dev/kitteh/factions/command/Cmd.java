package dev.kitteh.factions.command;

import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import dev.kitteh.factions.util.TriConsumer;

@FunctionalInterface
public interface Cmd {
    TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer();
}
