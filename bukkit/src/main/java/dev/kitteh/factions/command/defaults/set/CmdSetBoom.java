package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TriConsumer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.BooleanParser;

public class CmdSetBoom implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, _) -> {
            var tl = Confs.tl().commands().set().boom();
            manager.command(
                    builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                            .commandDescription(Cloudy.desc(tl.getDescription()))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.NO_BOOM).and(Cloudy.predicate(s -> s.hasFaction() && s.fPlayerOrNull().faction().isPeaceful())).and(Cloudy.isAtLeastRole(Role.MODERATOR))))
                            .optional("state", BooleanParser.booleanParser(true))
                            .handler(this::handle)
            );
        };
    }

    private void handle(org.incendo.cloud.context.CommandContext<Sender> context) {
        var tl = Confs.tl().commands().set().boom();
        var econTl = Confs.tl().economy().actions();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        if (!faction.isPeaceful()) {
            sender.sendRichMessage(tl.getPeacefulOnly());
            return;
        }

        if (!context.sender().payForCommand(Confs.main().economy().getCostNoBoom(), econTl.getBoomTo(), econTl.getBoomFor())) {
            return;
        }

        faction.peacefulExplosionsEnabled(context.getOrDefault("state", !faction.peacefulExplosionsEnabled()));

        String state = faction.peacefulExplosionsEnabled() ? "enabled" : "disabled";
        faction.sendRichMessage(tl.getEnabled(), FPlayerResolver.of("player", sender), Placeholder.unparsed("state", state));
    }
}
