package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdChatSpy implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("chatspy")
                            .commandDescription(Cloudy.desc(TL.COMMAND_CHATSPY_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.CHATSPY).and(Cloudy.isPlayer())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        sender.spyingChat(!sender.spyingChat());

        if (sender.spyingChat()) {
            sender.msg(TL.COMMAND_CHATSPY_ENABLE);
            FactionsPlugin.getInstance().log(sender.name() + TL.COMMAND_CHATSPY_ENABLELOG);
        } else {
            sender.msg(TL.COMMAND_CHATSPY_DISABLE);
            FactionsPlugin.getInstance().log(sender.name() + TL.COMMAND_CHATSPY_DISABLELOG);
        }
    }
}