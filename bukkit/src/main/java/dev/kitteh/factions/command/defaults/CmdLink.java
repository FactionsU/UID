package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import dev.kitteh.factions.util.TextUtil;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdLink implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> manager.command(
                builder.literal("link")
                        .commandDescription(Cloudy.desc(TL.COMMAND_LINK_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.LINK).and(Cloudy.hasFaction())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        sender.msgLegacy(TL.COMMAND_LINK_SHOW, TextUtil.getString(FactionsPlugin.instance().conf().colors().relations().getMember()), sender.faction().link());
    }
}
