package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FPlayerParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.data.MemoryFaction;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.util.BanInfo;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;
import java.util.logging.Level;

public class CmdBan implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("ban")
                            .commandDescription(Cloudy.desc(TL.COMMAND_BAN_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.BAN).and(Cloudy.hasSelfFactionPerms(PermissibleActions.BAN))))
                            .required("player", FPlayerParser.of(FPlayerParser.Include.SAME_FACTION, FPlayerParser.Include.ROLE_BELOW, FPlayerParser.Include.OTHER_FACTION))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        FPlayer target = context.get("player");
        Faction faction = sender.faction();

        if (sender == target) {
            // You may not ban yourself
            sender.msg(TL.COMMAND_BAN_SELF);
            return;
        } else if (target.faction() == faction && target.role().isAtLeast(sender.role())) {
            // You may not ban someone that has same or higher faction rank
            sender.msg(TL.COMMAND_BAN_INSUFFICIENTRANK, target.name());
            return;
        }

        // Check if the user is already banned
        if (faction.bans().stream().map(BanInfo::banned).anyMatch(u -> u.equals(target.uniqueId()))) {
            sender.msg(TL.COMMAND_BAN_ALREADYBANNED, target.name());
            return;
        }

        // If in same Faction, lets make sure to kick them and throw an event.
        if (target.faction() == faction) {
            FPlayerLeaveEvent event = new FPlayerLeaveEvent(target, faction, FPlayerLeaveEvent.Reason.BANNED);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                // if someone cancels a ban, we'll get people complaining here. So lets log it.
                FactionsPlugin.getInstance().log(Level.WARNING, "Attempted to ban {0} but a plugin cancelled the kick event.", target.name());
                return;
            }

            // Didn't get cancelled so remove them and reset their state.
            ((MemoryFaction) faction).removeMember(target);
            target.resetFactionData(true);
        }

        faction.ban(target, sender);
        faction.deInvite(target);

        target.msg(TL.COMMAND_BAN_TARGET, faction.tagString(target.faction()));
        faction.msg(TL.COMMAND_BAN_BANNED, sender.name(), target.name());
    }
}
