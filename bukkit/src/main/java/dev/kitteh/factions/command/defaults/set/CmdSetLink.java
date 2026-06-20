package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.util.Permission;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;

public class CmdSetLink implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            var tl = FactionsPlugin.instance().tl().commands().link();
            Command.Builder<Sender> build = builder.literal(tl.getFirstAlias(), tl.getSecondaryAliases())
                    .commandDescription(Cloudy.desc(tl.getDescription()))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.LINK).and(Cloudy.isAtLeastRole(Role.MODERATOR))));

            manager.command(
                    build.required("url", StringParser.greedyStringParser())
                            .handler(this::handle)
            );
            manager.command(build.meta(HIDE_IN_HELP, true)
                    .handler(ctx -> help.queryCommands(
                            Cmd.rootCommand() + " " + FactionsPlugin.instance().tl().commands().set().getFirstAlias() + " " + tl.getFirstAlias() + " <url>", ctx.sender())
                    ));
        };
    }

    private void handle(CommandContext<Sender> context) {
        var tl = FactionsPlugin.instance().tl().commands().link();
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String link = context.get("url");

        if (!sender.role().isAtLeast(Role.MODERATOR)) {
            sender.sendRichMessage(tl.getYouMustBeModerator());
            return;
        }

        if (!link.matches("^https?://.+")) {
            sender.sendRichMessage(tl.getInvalidUrl());
            return;
        }

        faction.link(link);

        faction.sendRichMessage(tl.getChanged(), FPlayerResolver.of("player", sender));
        faction.sendMessageLegacy(link);
    }
}
