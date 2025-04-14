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
        return (manager, builder) -> {
            manager.command(
                    builder.literal("kick")
                            .commandDescription(Cloudy.desc(TL.COMMAND_KICK_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.KICK_ANY)))
                            .required("target", FPlayerParser.of(FPlayerParser.Include.ALL))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        FPlayer toKick = context.get("target");

        Faction toKickFaction = toKick.getFaction();

        if (toKickFaction.isWilderness()) {
            sender.sendMessage(TL.COMMAND_KICK_NONE.toString());
            return;
        }

        // trigger the leave event (cancellable) [reason:kicked]
        FPlayerLeaveEvent event = new FPlayerLeaveEvent(toKick, toKick.getFaction(), FPlayerLeaveEvent.Reason.ADMIN_KICKED);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        toKickFaction.msg(TL.COMMAND_KICK_FACTION, sender.describeTo(toKickFaction, true), toKick.describeTo(toKickFaction, true));
        toKick.msg(TL.COMMAND_KICK_KICKED, sender.describeTo(toKick, true), toKickFaction.describeTo(toKick));

        if (FactionsPlugin.getInstance().conf().logging().isFactionKick()) {
            FactionsPlugin.getInstance().log(sender.getName() + " kicked " + toKick.getName() + " from the faction: " + toKickFaction.getTag());
        }

        if (toKick.getRole() == Role.ADMIN) {
            toKickFaction.promoteNewLeader();
        }

        toKickFaction.deinvite(toKick);
        toKick.resetFactionData(true);
    }
}
