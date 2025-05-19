package dev.kitteh.factions.command.defaults;

import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Factions;
import dev.kitteh.factions.FactionsPlugin;
import dev.kitteh.factions.command.Cloudy;
import dev.kitteh.factions.command.Cmd;
import dev.kitteh.factions.command.Sender;
import dev.kitteh.factions.event.FPlayerLeaveEvent;
import dev.kitteh.factions.event.FactionDisbandEvent;
import dev.kitteh.factions.integration.Econ;
import dev.kitteh.factions.permissible.PermissibleActions;
import dev.kitteh.factions.scoreboard.FTeamWrapper;
import dev.kitteh.factions.util.Permission;
import dev.kitteh.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.incendo.cloud.Command;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

import java.util.function.BiConsumer;

public class CmdDisband implements Cmd {
    @Override
    public BiConsumer<CommandManager<Sender>, Command.Builder<Sender>> consumer() {
        return (manager, builder) -> {
            manager.command(
                    builder.literal("disband")
                            .commandDescription(Cloudy.desc(TL.COMMAND_DISBAND_DESCRIPTION))
                            .permission(builder.commandPermission().and(Cloudy.hasPermission(Permission.DISBAND).and(Cloudy.hasSelfFactionPerms(PermissibleActions.DISBAND))))
                            .handler(this::handle)
            );
        };
    }

    private void handle(CommandContext<Sender> context) {
        FPlayer sender = ((Sender.Player) context.sender()).fPlayer();
        this.doIt(sender, false);
    }

    private void doIt(FPlayer sender, boolean confirmed) {
        Faction faction = sender.faction();

        if (!faction.hasAccess(sender, PermissibleActions.DISBAND, sender.lastStoodAt())) {
            sender.msg(TL.GENERIC_NOPERMISSION.format(PermissibleActions.DISBAND.shortDescription()));
            return;
        }

        if (faction.permanent()) {
            sender.msg(TL.COMMAND_DISBAND_MARKEDPERMANENT.toString());
            return;
        }

        if (!FactionsPlugin.instance().landRaidControl().canDisbandFaction(faction, sender)) {
            return;
        }

        if (!confirmed) {
            String conf = CmdConfirm.add(sender, s -> this.doIt(s, true));
            // TODO TL
            sender.sendMessage(ChatColor.YELLOW + "Are you sure you want to disband the faction? If so, run /f confirm " + conf);
            return;
        }

        FactionDisbandEvent disbandEvent = new FactionDisbandEvent(sender, faction);
        Bukkit.getServer().getPluginManager().callEvent(disbandEvent);
        if (disbandEvent.isCancelled()) {
            return;
        }

        // Send FPlayerLeaveEvent for each player in the faction
        for (FPlayer fplayer : faction.members()) {
            Bukkit.getServer().getPluginManager().callEvent(new FPlayerLeaveEvent(fplayer, faction, FPlayerLeaveEvent.Reason.DISBAND));
        }

        // Inform all players
        for (FPlayer fplayer : FPlayers.fPlayers().online()) {
            String who = sender.describeTo(fplayer);
            if (fplayer.faction() == faction) {
                fplayer.msg(TL.COMMAND_DISBAND_BROADCAST_YOURS, who);
            } else {
                fplayer.msg(TL.COMMAND_DISBAND_BROADCAST_NOTYOURS, who, faction.tagString(fplayer));
            }
        }
        if (FactionsPlugin.instance().conf().logging().isFactionDisband()) {
            FactionsPlugin.instance().log("The faction " + faction.tag() + " (" + faction.id() + ") was disbanded by " + sender.name() + ".");
        }

        if (Econ.shouldBeUsed() && FactionsPlugin.instance().conf().economy().isBankEnabled()) {
            //Give all the faction's money to the disbander
            double amount = Econ.getBalance(faction);

            if (amount > 0.0) {
                Econ.transferMoney(sender, faction, sender, amount, false);
                String amountString = Econ.moneyString(amount);
                sender.msg(TL.COMMAND_DISBAND_HOLDINGS, amountString);
                FactionsPlugin.instance().log(sender.name() + " has been given bank holdings of " + amountString + " from disbanding " + faction.tag() + ".");
            }
        }

        Factions.factions().remove(faction);
        FTeamWrapper.applyUpdates(faction);
    }
}
