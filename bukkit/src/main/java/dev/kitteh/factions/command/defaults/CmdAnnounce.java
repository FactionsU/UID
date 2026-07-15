package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.integration.ExternalChecks;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Mini;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.StringParser;

public class CmdAnnounce implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().announce();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.ANNOUNCE).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                            .required("message", StringParser.greedyStringParser())
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        if (ExternalChecks.isMuted(((Sender.Player) context.sender()).player())) {
            return;
        }

        var tl = Confs.tl().commands().announce();

        FactionResolver factionResolver = FactionResolver.of(faction);
        FPlayerResolver fPlayerResolver = FPlayerResolver.of("player", sender);
        TagResolver message = Placeholder.unparsed("message", context.get("message"));

        faction.sendRichMessage(tl.getFormat(), message, factionResolver, fPlayerResolver);

        // Add for offline players.
        for (FPlayer fp : faction.membersOnline(false)) {
            faction.addAnnouncement(fp, Mini.parse(tl.getFormat(), fp, message, factionResolver, fPlayerResolver));
        }
    }
}
