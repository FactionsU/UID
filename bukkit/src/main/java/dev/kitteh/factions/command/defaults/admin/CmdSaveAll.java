package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.Instances;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSaveAll implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("save-all")
                        .commandDescription(Cloudy.desc(TL.COMMAND_SAVEALL_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SAVE)))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Instances.PLAYERS.forceSave(false);
        Instances.FACTIONS.forceSave(false);
        Instances.BOARD.forceSave(false);
        Instances.UNIVERSE.forceSave(false);
        context.sender().msgLegacy(TL.COMMAND_SAVEALL_SUCCESS);
    }
}