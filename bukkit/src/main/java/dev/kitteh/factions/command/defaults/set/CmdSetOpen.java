package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.tagresolver.FactionResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.BooleanParser;

public class CmdSetOpen implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().set().open();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.OPEN).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                            .optional("openstate", BooleanParser.booleanParser(true))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().open();
        var econTl = Confs.tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        if (!context.sender().payForCommand(Confs.main().economy().getCostOpen(), econTl.getOpenTo(), econTl.getOpenFor())) {
            return;
        }

        faction.open(context.getOrDefault("openstate", !faction.open()));

        String open = faction.open() ? tl.getOpen() : tl.getClosed();

        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            if (fplayer.faction() == faction) {
                fplayer.sendRichMessage(tl.getChanges(),
                        FPlayerResolver.of("player", sender),
                        Placeholder.unparsed("state", open));
                continue;
            }
            fplayer.sendRichMessage(tl.getChanged(),
                    FactionResolver.of(faction),
                    Placeholder.unparsed("state", open));
        }
    }
}
