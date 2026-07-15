package dev.kitteh.factions.command.defaults.toggle;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdToggleSeeChunk implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        var tl = Confs.tl().commands().toggleSeeChunk();
        return (manager, builder, _) -> manager.command(
                builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                        .commandDescription(Cloudy.desc(tl.getDescription()))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SEECHUNK).and(Cloudy.isPlayer())))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().toggleSeeChunk();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        boolean toggle = !sender.seeChunk();
        sender.seeChunk(toggle);
        sender.sendRichMessage(tl.getToggle(), Placeholder.unparsed("state", toggle ? "enabled" : "disabled"));
    }
}
