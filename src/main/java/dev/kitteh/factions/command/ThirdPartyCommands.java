package dev.kitteh.factions.command;

import org.bukkit.plugin.Plugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.jspecify.annotations.NullMarked;

import java.util.function.BiConsumer;

/**
 * For the registry of commands from other plugins.
 */
@SuppressWarnings("unused")
@NullMarked
public final class ThirdPartyCommands {
    private ThirdPartyCommands() {
    }

    /**
     * Registers a command for the main faction command, to be called during load.
     *
     * @param providingPlugin your plugin, for tracking in exceptions
     * @param command         command name, for tracking in exceptions
     * @param consumer        a consumer of the command manager and the builder for the faction command, to build from. The
     *                        consumer will be called during FactionsUUID's onEnable
     * @throws IllegalArgumentException for not using your plugin
     * @throws IllegalStateException    if attempting after registration has closed
     */
    static void register(Plugin providingPlugin, String command, BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer) {
        CommandsRoot.register(providingPlugin, command, consumer);
    }

    /**
     * Registers a command for the faction admin command, to be called during load.
     *
     * @param providingPlugin your plugin, for tracking in exceptions
     * @param command         command name, for tracking in exceptions
     * @param consumer        a consumer of the command manager and the builder for the faction admin command, to build from. The
     *                        consumer will be called during FactionsUUID's onEnable
     * @throws IllegalArgumentException for not using your plugin
     * @throws IllegalStateException    if attempting after registration has closed
     */
    static void registerAdmin(Plugin providingPlugin, String command, BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer) {
        CommandsRoot.registerAdmin(providingPlugin, command, consumer);
    }
}
