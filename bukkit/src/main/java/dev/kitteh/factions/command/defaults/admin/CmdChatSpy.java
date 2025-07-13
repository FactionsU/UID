package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdChatSpy implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("chatspy")
                        .commandDescription(Cloudy.desc(TL.COMMAND_CHATSPY_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.CHATSPY).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        sender.spyingChat(!sender.spyingChat());

        if (sender.spyingChat()) {
            sender.msgLegacy(TL.COMMAND_CHATSPY_ENABLE);
            AbstractFactionsPlugin.instance().log(sender.name() + TL.COMMAND_CHATSPY_ENABLELOG);
        } else {
            sender.msgLegacy(TL.COMMAND_CHATSPY_DISABLE);
            AbstractFactionsPlugin.instance().log(sender.name() + TL.COMMAND_CHATSPY_DISABLELOG);
        }
    }
}