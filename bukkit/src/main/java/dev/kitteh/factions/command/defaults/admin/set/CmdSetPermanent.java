package dev.kitteh.factions.command.defaults.admin.set;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdSetPermanent implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("permanent")
                        .commandDescription(Cloudy.desc(TL.COMMAND_PERMANENT_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.SET_PERMANENT)))
                        .required("faction", FactionParser.of(FactionParser.Include.SELF))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        Faction faction = context.get("faction");

        String change;
        if (faction.isPermanent()) {
            change = TL.COMMAND_PERMANENT_REVOKE.toString();
            faction.permanent(false);
        } else {
            change = TL.COMMAND_PERMANENT_GRANT.toString();
            faction.permanent(true);
        }

        FPlayer fPlayer = context.sender().fPlayerOrNull();

        AbstractFactionsPlugin.instance().log((fPlayer == null ? "A server admin" : fPlayer.name()) + " " + change + " the faction \"" + faction.tag() + "\".");

        // Inform all players
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            String blame = (fPlayer == null ? TL.GENERIC_SERVERADMIN.toString() : fPlayer.describeToLegacy(fplayer));
            if (fplayer.faction() == faction) {
                fplayer.msgLegacy(TL.COMMAND_PERMANENT_YOURS, blame, change);
            } else {
                fplayer.msgLegacy(TL.COMMAND_PERMANENT_OTHER, blame, change, faction.tagLegacy(fplayer));
            }
        }
    }
}
