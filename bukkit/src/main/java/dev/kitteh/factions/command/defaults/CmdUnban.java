package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import dev.kitteh.factions.util.TriConsumer;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.parser.standard.IntegerParser;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;

public class CmdUnban implements Cmd {
    @Override
    public TriConsumer<CommandManager<Sender>, Command.Builder<Sender>, MinecraftHelp<Sender>> consumer() {
        return (manager, builder, help) -> {
            Command.Builder<Sender> build = builder.literal("unban")
                    .commandDescription(Cloudy.desc(TL.COMMAND_UNBAN_DESCRIPTION))
                    .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasSelfFactionPerms(PermissibleActions.BAN))));

            manager.command(
                    build.required("player", FPlayerParser.of(FPlayerParser.Include.BANNED))
                            .handler(this::handle)
            );
            manager.command(build.meta(HIDE_IN_HELP, true).handler(ctx -> help.queryCommands("f unban <player>", ctx.sender())));
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        Faction faction = sender.faction();

        FPlayer target = context.get("player");

        if (!faction.isBanned(target)) {
            sender.msgLegacy(TL.COMMAND_UNBAN_NOTBANNED, target.name());
            return;
        }

        faction.unban(target);

        faction.msgLegacy(TL.COMMAND_UNBAN_UNBANNED, sender.name(), target.name());
        target.msgLegacy(TL.COMMAND_UNBAN_TARGET, faction.tagLegacy(target));
    }
}
