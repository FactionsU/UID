package com.massivecraft.factions.landraidcontrol;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.FactionsPlugin;
import com.massivecraft.factions.cmd.CommandContext;
import com.massivecraft.factions.event.PowerLossEvent;
import com.massivecraft.factions.util.TL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PowerControl implements LandRaidControl {
    @Override
    public boolean isRaidable(Faction faction) {
        return FactionsPlugin.getInstance().conf().factions().landRaidControl().power().isRaidability() && faction.getLandRounded() >= faction.getPowerRounded();
    }

    @Override
    public boolean hasLandInflation(Faction faction) {
        return faction.getLandRounded() > faction.getPowerRounded();
    }

    @Override
    public int getLandLimit(Faction faction) {
        return faction.getPowerRounded();
    }

    @Override
    public boolean canJoinFaction(Faction faction, FPlayer player, CommandContext context) {
        if (!FactionsPlugin.getInstance().conf().factions().landRaidControl().power().canLeaveWithNegativePower() && player.getPower() < 0) {
            if (context != null) {
                context.msg(TL.COMMAND_JOIN_NEGATIVEPOWER, player.describeTo(context.fPlayer, true));
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean canLeaveFaction(FPlayer player) {
        if (!FactionsPlugin.getInstance().conf().factions().landRaidControl().power().canLeaveWithNegativePower() && player.getPower() < 0) {
            player.msg(TL.LEAVE_NEGATIVEPOWER);
            return false;
        }
        return true;
    }

    @Override
    public boolean canDisbandFaction(Faction faction, CommandContext context) {
        return true;
    }

    @Override
    public boolean canKick(FPlayer toKick, CommandContext context) {
        if (!FactionsPlugin.getInstance().conf().factions().landRaidControl().power().canLeaveWithNegativePower() && toKick.getPower() < 0) {
            context.msg(TL.COMMAND_KICK_NEGATIVEPOWER);
            return false;
        }
        return true;
    }

    @Override
    public void onRespawn(FPlayer player) {
        this.update(player); // update power, so they won't have gained any while dead
    }

    @Override
    public void onQuit(FPlayer player) {
        this.update(player); // Make sure player's power is up to date when they log off.
    }

    @Override
    public void update(FPlayer player) {
        player.updatePower();
    }

    @Override
    public void onJoin(FPlayer player) {
        player.losePowerFromBeingOffline();
    }

    @Override
    public void onDeath(Player player) {
        FPlayer fplayer = FPlayers.getInstance().getByPlayer(player);
        Faction faction = Board.getInstance().getFactionAt(new FLocation(player.getLocation()));

        PowerLossEvent powerLossEvent = new PowerLossEvent(faction, fplayer);
        // Check for no power loss conditions
        if (faction.isWarZone()) {
            // war zones always override worldsNoPowerLoss either way, thus this layout
            if (!FactionsPlugin.getInstance().conf().factions().landRaidControl().power().isWarZonePowerLoss()) {
                powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_WARZONE.toString());
                powerLossEvent.setCancelled(true);
            }
            if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getWorldsNoPowerLoss().contains(player.getWorld().getName())) {
                powerLossEvent.setMessage(TL.PLAYER_POWER_LOSS_WARZONE.toString());
            }
        } else if (faction.isWilderness() && !FactionsPlugin.getInstance().conf().factions().landRaidControl().power().isWildernessPowerLoss() && !FactionsPlugin.getInstance().conf().factions().protection().getWorldsNoWildernessProtection().contains(player.getWorld().getName())) {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_WILDERNESS.toString());
            powerLossEvent.setCancelled(true);
        } else if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getWorldsNoPowerLoss().contains(player.getWorld().getName())) {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_WORLD.toString());
            powerLossEvent.setCancelled(true);
        } else if (FactionsPlugin.getInstance().conf().factions().landRaidControl().power().isPeacefulMembersDisablePowerLoss() && fplayer.hasFaction() && fplayer.getFaction().isPeaceful()) {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOLOSS_PEACEFUL.toString());
            powerLossEvent.setCancelled(true);
        } else {
            powerLossEvent.setMessage(TL.PLAYER_POWER_NOW.toString());
        }

        // call Event
        Bukkit.getPluginManager().callEvent(powerLossEvent);

        fplayer.onDeath();
        if (!powerLossEvent.isCancelled()) {
            fplayer.alterPower(-FactionsPlugin.getInstance().conf().factions().landRaidControl().power().getLossPerDeath());
        }
        // Send the message from the powerLossEvent
        final String msg = powerLossEvent.getMessage();
        if (msg != null && !msg.isEmpty()) {
            fplayer.msg(msg, fplayer.getPowerRounded(), fplayer.getPowerMaxRounded());
        }
    }
}
