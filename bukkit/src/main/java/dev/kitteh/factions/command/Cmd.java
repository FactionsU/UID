package dev.kitteh.factions.command;

import dev.kitteh.factions.FactionsPlugin;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

import dev.kitteh.factions.util.TriConsumer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@FunctionalInterface
public interface Cmd {
    CloudKey<Boolean> HIDE_IN_HELP = CloudKey.cloudKey("HIDE_IN_HELP", Boolean.class);

    TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer();

    /**
     * Helper method for getting the primary root command string, by default 'f'.
     *
     * @return primary root command
     */
    static String rootCommand() {
        return FactionsPlugin.instance().tl().commands().generic().getCommandRoot().getFirstAlias();
    }

    /**
     * Helper method for getting the primary root admin command string, by default 'f'.
     *
     * @return primary root admin command
     */
    static String rootAdminCommand() {
        return FactionsPlugin.instance().tl().commands().generic().getCommandAdminRoot().getFirstAlias();
    }
}
