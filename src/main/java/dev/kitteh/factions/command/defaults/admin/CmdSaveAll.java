package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.data.MemoryBoard;
import dev.kitteh.factions.data.MemoryFPlayers;
import dev.kitteh.factions.data.MemoryFactions;
import dev.kitteh.factions.data.MemoryUniverse;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdSaveAll implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("save-all")
                            .commandDescription(Cloudy.desc(TL.COMMAND_SAVEALL_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SAVE)))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        ((MemoryFPlayers) FPlayers.fPlayers()).forceSave(false);
        ((MemoryFactions) Factions.factions()).forceSave(false);
        ((MemoryBoard) Board.board()).forceSave(false);
        ((MemoryUniverse) Universe.universe()).forceSave(false);
        context.sender().msg(TL.COMMAND_SAVEALL_SUCCESS);
    }
}