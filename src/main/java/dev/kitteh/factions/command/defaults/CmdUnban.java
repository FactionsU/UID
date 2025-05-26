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

import java.util.function.BiConsumer;

public class CmdUnban implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("unban")
                        .commandDescription(Cloudy.desc(TL.COMMAND_UNBAN_DESCRIPTION))
                        .required("player", FPlayerParser.of(FPlayerParser.Include.BANNED))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasSelfFactionPerms(PermissibleActions.BAN))))
                        .handler(this::handle)
        );
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
