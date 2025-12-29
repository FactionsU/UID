package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
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
            var tl = FactionsPlugin.instance().tl().commands().list();
            Command.Builder<Sender> listBuilder = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()));

            new CmdListBans().consumer().accept(manager, listBuilder, help);
            new CmdListClaims().consumer().accept(manager, listBuilder, help);
            new CmdListFactions().consumer().accept(manager, listBuilder, help);
            new CmdListInvites().consumer().accept(manager, listBuilder, help);
        };
    }
}
