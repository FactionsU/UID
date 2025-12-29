package dev.kitteh.factions.command.defaults.admin;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdChatSpy implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().admin().chatSpy();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.CHATSPY).and(Cloudy.isPlayer())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        sender.spyingChat(!sender.spyingChat());

        var tl = FactionsPlugin.instance().tl().commands().admin().chatSpy();
        if (sender.spyingChat()) {
            sender.sendRichMessage(tl.getEnable());
            AbstractFactionsPlugin.instance().log(sender.name() + " enabled chat spying mode.");
        } else {
            sender.sendRichMessage(tl.getDisable());
            AbstractFactionsPlugin.instance().log(sender.name() + " disabled chat spying mode.");
        }
    }
}