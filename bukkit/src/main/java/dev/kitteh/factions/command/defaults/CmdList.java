package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.command.defaults.list.CmdListBans;
import dev.kitteh.factions.command.defaults.list.CmdListClaims;
import dev.kitteh.factions.command.defaults.list.CmdListFactions;
import dev.kitteh.factions.command.defaults.list.CmdListInvites;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdList implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> listBuilder = builder.literal("list");

            new CmdListBans().consumer().accept(manager, listBuilder, help);
            new CmdListClaims().consumer().accept(manager, listBuilder, help);
            new CmdListFactions().consumer().accept(manager, listBuilder, help);
            new CmdListInvites().consumer().accept(manager, listBuilder, help);
        };
    }
}
