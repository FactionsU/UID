package dev.kitteh.factions.command.defaults.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
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
            Command.Builder<Sender> linkBuilder = builder.literal("link")
                    .commandDescription(Cloudy.desc(TL.COMMAND_LINK_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.LINK).and(Cloudy.isAtLeastRole(Role.MODERATOR))));

            manager.command(
                    linkBuilder.required("url", StringParser.greedyStringParser())
                            .handler(this::handle)
            );
            manager.command(linkBuilder.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f set link <url>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        String link = context.get("url");

        if (!sender.role().isAtLeast(Role.MODERATOR)) {
            sender.msgLegacy(TL.GENERIC_YOUMUSTBE, Role.MODERATOR.translation);
            return;
        }

        if (!link.matches("^https?://.+")) {
            sender.msgLegacy(TL.COMMAND_LINK_INVALIDURL);
            return;
        }

        faction.link(link);

        faction.msgLegacy(TL.COMMAND_LINK_CHANGED, sender.describeToLegacy(faction));
        faction.sendMessageLegacy(link);
    }
}
