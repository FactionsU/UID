package dev.kitteh.factions.command.defaults.admin.force;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.permissible.Role;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdForceKick implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> manager.command(
                builder.literal("kick")
                        .commandDescription(Cloudy.desc(TL.COMMAND_KICK_DESCRIPTION))
                        .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.KICK_ANY)))
                        .required("target", FPlayerParser.of(FPlayerParser.Include.ALL))
                        .handler(this::handle)
        );
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        FPlayer toKick = context.get("target");

        Faction toKickFaction = toKick.faction();

        if (toKickFaction.isWilderness()) {
            sender.sendMessage(TL.COMMAND_KICK_NONE.toString());
            return;
        }

        // trigger the leave event (cancellable) [reason:kicked]
        FPlayerLeaveEvent event = new FPlayerLeaveEvent(toKick, toKick.faction(), FPlayerLeaveEvent.Reason.ADMIN_KICKED);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        toKickFaction.msg(TL.COMMAND_KICK_FACTION, sender.describeToLegacy(toKickFaction, true), toKick.describeToLegacy(toKickFaction, true));
        toKick.msg(TL.COMMAND_KICK_KICKED, sender.describeToLegacy(toKick, true), toKickFaction.describeToLegacy(toKick));

        if (FactionsPlugin.instance().conf().logging().isFactionKick()) {
            AbstractFactionsPlugin.instance().log(sender.name() + " kicked " + toKick.name() + " from the faction: " + toKickFaction.tag());
        }

        if (toKick.role() == Role.ADMIN) {
            toKickFaction.promoteNewLeader();
        }

        toKickFaction.deInvite(toKick);
        toKick.resetFactionData(true);
    }
}
