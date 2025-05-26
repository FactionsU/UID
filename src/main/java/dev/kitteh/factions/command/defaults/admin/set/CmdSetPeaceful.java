package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdSetPeaceful implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("peaceful")
                        .commandDescription(Cloudy.desc(TL.COMMAND_PEACEFUL_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SET_PEACEFUL)))
                        .required("faction", FactionParser.of(FactionParser.Include.SELF))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Faction faction = context.get("faction");
        FPlayer fPlayer = context.sender().fPlayerOrNull();

        String change;
        if (faction.peaceful()) {
            change = TL.COMMAND_PEACEFUL_REVOKE.toString();
            faction.peaceful(false);
        } else {
            change = TL.COMMAND_PEACEFUL_GRANT.toString();
            faction.peaceful(true);
        }

        // Inform all players
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            String blame = (fPlayer == null ? TL.GENERIC_SERVERADMIN.toString() : fPlayer.describeToLegacy(fplayer, true));
            if (fplayer.faction() == faction) {
                fplayer.msg(TL.COMMAND_PEACEFUL_YOURS, blame, change);
            } else {
                fplayer.msg(TL.COMMAND_PEACEFUL_OTHER, blame, change, faction.tagLegacy(fplayer));
            }
        }
    }
}
