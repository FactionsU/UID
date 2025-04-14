package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.FactionParser;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerJoinEvent;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdJoin implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("join")
                            .commandDescription(Cloudy.desc(TL.COMMAND_JOIN_DESCRIPTION))
                            .required("faction", FactionParser.of())
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.JOIN).and(Cloudy.isPlayer())))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();

        Faction faction = context.get("faction");

        if (!faction.isNormal()) {
            sender.msg(TL.COMMAND_JOIN_SYSTEMFACTION);
            return;
        }

        if (faction == sender.getFaction()) {
            //TODO:TL
            sender.msg(TL.COMMAND_JOIN_ALREADYMEMBER, sender.describeTo(sender, false), "is", faction.getTag(sender));
            return;
        }

        if (FactionsPlugin.getInstance().conf().factions().other().getFactionMemberLimit() > 0 && faction.getFPlayers().size() >= FactionsPlugin.getInstance().conf().factions().other().getFactionMemberLimit()) {
            sender.msg(TL.COMMAND_JOIN_ATLIMIT, faction.getTag(sender), FactionsPlugin.getInstance().conf().factions().other().getFactionMemberLimit(), sender.describeTo(sender, false));
            return;
        }

        if (sender.hasFaction()) {
            //TODO:TL
            sender.msg(TL.COMMAND_JOIN_INOTHERFACTION, sender.describeTo(sender, true), "your");
            return;
        }

        if (!FactionsPlugin.getInstance().getLandRaidControl().canJoinFaction(faction, sender)) {
            return;
        }

        if (!(faction.getOpen() || faction.isInvited(sender))) {
            sender.msg(TL.COMMAND_JOIN_REQUIRESINVITATION);
            if (!faction.isBanned(sender)) {
                faction.msg(TL.COMMAND_JOIN_ATTEMPTEDJOIN, sender.describeTo(faction, true));
            }
            return;
        }

        // if economy is enabled, they're not on the bypass list, and this command has a cost set, make sure they can pay
        if (!context.sender().canAffordCommand(FactionsPlugin.getInstance().conf().economy().getCostJoin(), TL.COMMAND_JOIN_TOJOIN)) {
            return;
        }

        // Check for ban
        if (faction.isBanned(sender)) {
            sender.msg(TL.COMMAND_JOIN_BANNED, faction.getTag(sender));
            return;
        }

        // trigger the join event (cancellable)
        FPlayerJoinEvent joinEvent = new FPlayerJoinEvent(sender, faction, FPlayerJoinEvent.Reason.COMMAND);
        Bukkit.getServer().getPluginManager().callEvent(joinEvent);
        if (joinEvent.isCancelled()) {
            return;
        }

        // then make 'em pay (if applicable)
        if (!context.sender().payForCommand(FactionsPlugin.getInstance().conf().economy().getCostJoin(), TL.COMMAND_JOIN_TOJOIN, TL.COMMAND_JOIN_FORJOIN)) {
            return;
        }

        sender.msg(TL.COMMAND_JOIN_SUCCESS, sender.describeTo(sender, true), faction.getTag(sender));

        faction.msg(TL.COMMAND_JOIN_JOINED, sender.describeTo(faction, true));

        sender.resetFactionData();
        sender.setFaction(faction);
        faction.deinvite(sender);
        sender.setRole(faction.getDefaultRole());
        sender.getPlayer().updateCommands();

        if (FactionsPlugin.getInstance().conf().logging().isFactionJoin()) {
                FactionsPlugin.getInstance().log(TL.COMMAND_JOIN_JOINEDLOG.toString(), sender.getName(), faction.getTag());
        }
    }
}
