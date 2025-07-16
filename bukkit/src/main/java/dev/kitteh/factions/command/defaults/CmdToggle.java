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

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdToggle implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> toggleBuilder = builder.literal("toggle").permission(builder.commandPermission().and(Cloudy.hasFaction()));
            new CmdToggleChat().consumer().accept(manager, toggleBuilder, help);
            new CmdToggleSeeChunk().consumer().accept(manager, toggleBuilder, help);
            new CmdToggleLogins().consumer().accept(manager, toggleBuilder, help);
            new CmdToggleScoreboard().consumer().accept(manager, toggleBuilder, help);

            manager.command(toggleBuilder.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f toggle *", ctx.sender())));
        };
    }
}
