package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.list.CmdListBans;
import dev.kitteh.factions.command.defaults.list.CmdListClaims;
import dev.kitteh.factions.command.defaults.list.CmdListFactions;
import dev.kitteh.factions.command.defaults.list.CmdListInvites;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import java.util.function.BiConsumer;

public class CmdList implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            Command.Builder<Sender> listBuilder = builder.literal("list");

            new CmdListBans().consumer().accept(manager, listBuilder);
            new CmdListClaims().consumer().accept(manager, listBuilder);
            new CmdListFactions().consumer().accept(manager, listBuilder);
            new CmdListInvites().consumer().accept(manager, listBuilder);
        };
    }
}
