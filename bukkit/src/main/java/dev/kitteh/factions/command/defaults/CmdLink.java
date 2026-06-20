package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdLink implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        var tl = FactionsPlugin.instance().tl().commands().link();
        return (manager, builder, _) -> manager.command(
                builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                        .commandDescription(Cloudy.desc(tl.getDescription()))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.LINK).and(Cloudy.hasFaction())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().link();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        sender.sendRichMessage(tl.getShow(), FactionResolver.of(sender.faction()), Placeholder.unparsed("url", sender.faction().link()));
    }
}
