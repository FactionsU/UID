package dev.kitteh.factions.landraidcontrol;

import dev.kitteh.factions.Board;
import dev.kitteh.factions.FLocation;
import dev.kitteh.factions.FPlayer;
import dev.kitteh.factions.FPlayers;
import dev.kitteh.factions.Faction;
import dev.kitteh.factions.Universe;
import dev.kitteh.factions.config.Confs;
import dev.kitteh.factions.config.file.MainConfig;
import dev.kitteh.factions.event.PowerLossEvent;
import dev.kitteh.factions.permissible.Relation;
import dev.kitteh.factions.plugin.AbstractFactionsPlugin;
import dev.kitteh.factions.tagresolver.FPlayerResolver;
import dev.kitteh.factions.upgrade.Upgrades;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;

@ApiStatus.AvailableSince("4.0.0")
@NullMarked
public final class PowerControl implements LandRaidControl {
    @Override
    public boolean isRaidable(Faction faction) {
        return this.isRaidable(faction, faction.power());
    }

    public boolean isRaidable(Faction faction, int power) {
        return Confs.main().factions().landRaidControl().power().isRaidability() && faction.isNormal() && !faction.isPeaceful() &&
                (Confs.main().factions().landRaidControl().power().isRaidabilityOnEqualLandAndPower() ?
                        (faction.claimCount() >= power) :
                        (faction.claimCount() > power)
                );
    }

    @Override
    public boolean hasLandInflation(Faction faction) {
        return !faction.isPeaceful() && faction.claimCount() > faction.power();
    }

    @Override
    public int landLimit(Faction faction) {
        return faction.power();
    }

    @Override
    public boolean canJoinFaction(Faction faction, FPlayer player) {
        if (!Confs.main().factions().landRaidControl().power().canLeaveWithNegativePower() && player.power() < 0) {
            player.sendRichMessage(Confs.tl().commands().join().getNegativePower(), Placeholder.unparsed("player", player.name()));
            return false;
        }
        return true;
    }

    @Override
    public boolean canLeaveFaction(FPlayer player) {
        if (!Confs.main().factions().landRaidControl().power().canLeaveWithNegativePower() && player.power() < 0) {
            player.sendRichMessage(Confs.tl().commands().leave().getNegativePower());
            return false;
        }
        return true;
    }

    @Override
    public boolean canDisbandFaction(Faction faction, FPlayer playerAttempting) {
        return true;
    }

    @Override
    public boolean canKick(FPlayer toKick, FPlayer playerAttempting) {
        var kickTl = Confs.tl().commands().kick();
        if (!Confs.main().factions().landRaidControl().power().canLeaveWithNegativePower() && toKick.power() < 0) {
            playerAttempting.sendRichMessage(kickTl.getNegativePower());
            return false;
        }
        if (toKick.isOnline() && !Confs.main().commands().kick().isAllowKickInEnemyTerritory() &&
                Board.board().factionAt(toKick.lastStoodAt()).relationTo(toKick.faction()) == Relation.ENEMY) {
            playerAttempting.sendRichMessage(kickTl.getEnemyTerritory());
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
        FPlayer fplayer = FPlayers.fPlayers().get(player);
        Faction faction = Board.board().factionAt(new FLocation(player.getLocation()));

        MainConfig.Factions.LandRaidControl.Power powerConf = Confs.main().factions().landRaidControl().power();
        PowerLossEvent powerLossEvent = new PowerLossEvent(faction, fplayer);
        // Check for no power loss conditions
        var power = Confs.tl().landRaid().power();
        if (AbstractFactionsPlugin.instance().getWorldguard() != null && AbstractFactionsPlugin.instance().getWorldguard().isNoLossFlag(player)) {
            powerLossEvent.setMessage(power.getNoPowerLossRegion());
            powerLossEvent.setCancelled(true);
        } else if (faction.isWarZone()) {
            // war zones always override worldsNoPowerLoss either way, thus this layout
            if (!powerConf.isWarZonePowerLoss()) {
                powerLossEvent.setMessage(power.getNoPowerLossWarzone());
                powerLossEvent.setCancelled(true);
            }
            if (powerConf.getWorldsNoPowerLoss().contains(player.getWorld().getName())) {
                powerLossEvent.setMessage(power.getPowerLossWarzone());
            }
        } else if (faction.isWilderness() && !powerConf.isWildernessPowerLoss() && !Confs.main().factions().protection().getWorldsNoWildernessProtection().contains(player.getWorld().getName())) {
            powerLossEvent.setMessage(power.getNoPowerLossWilderness());
            powerLossEvent.setCancelled(true);
        } else if (powerConf.getWorldsNoPowerLoss().contains(player.getWorld().getName())) {
            powerLossEvent.setMessage(power.getNoPowerLossWorld());
            powerLossEvent.setCancelled(true);
        } else if (powerConf.isPeacefulMembersDisablePowerLoss() && fplayer.hasFaction() && fplayer.faction().isPeaceful()) {
            powerLossEvent.setMessage(power.getNoPowerLossPeaceful());
            powerLossEvent.setCancelled(true);
        } else {
            powerLossEvent.setMessage(power.getPowerNow());
        }

        // call Event
        Bukkit.getPluginManager().callEvent(powerLossEvent);

        fplayer.onDeath();
        if (!powerLossEvent.isCancelled()) {
            double startingPower = fplayer.power();
            double fullLoss = powerConf.getLossPerDeath();
            double loss = fullLoss;

            int reductionLevel = fplayer.faction().upgradeLevel(Upgrades.POWER_LOSS_REDUCTION);
            if (reductionLevel > 0) {
                double reduction = Universe.universe().upgradeSettings(Upgrades.POWER_LOSS_REDUCTION).valueAt(Upgrades.Variables.PERCENT, reductionLevel).doubleValue();
                reduction = Math.clamp(reduction, 0, 1);
                loss *= (1 - reduction);
            }

            fplayer.alterPower(-loss);

            double vampLoss;
            if (powerConf.isPowerLossReductionAffectsVampirism()) {
                vampLoss = startingPower - fplayer.power();
            } else {
                vampLoss = Math.clamp(fullLoss, 0, startingPower - fplayer.powerMin());
            }

            double vamp = powerConf.getVampirism();
            Player killer = player.getKiller();
            if (killer != null && vamp != 0D && vampLoss > 0) {
                double powerChange = vamp * vampLoss;
                FPlayer fKiller = FPlayers.fPlayers().get(killer);
                fKiller.alterPower(powerChange);
                fKiller.sendRichMessage(Confs.tl().landRaid().power().getVampirismGain(),
                        Placeholder.unparsed("amount", String.format("%.2f", powerChange)),
                        FPlayerResolver.of("player", fplayer),
                        Placeholder.unparsed("power", String.valueOf(fKiller.powerRounded())),
                        Placeholder.unparsed("maxpower", String.valueOf(fKiller.powerMaxRounded())));
            }
        }
        // Send the message from the powerLossEvent
        final String msg = powerLossEvent.getMessage();
        if (msg != null && !msg.isEmpty()) {
            fplayer.sendRichMessage(msg,
                    Placeholder.unparsed("power", String.valueOf(fplayer.powerRounded())),
                    Placeholder.unparsed("maxpower", String.valueOf(fplayer.powerMaxRounded())));
        }
    }

    public void onPowerChange(Faction faction, int start, int end) {
        boolean raidStart = this.isRaidable(faction, start);
        boolean raidEnd = this.isRaidable(faction, end);
        if (raidEnd && !raidStart) {
            this.announceRaidable(faction);
        } else if (raidStart && !raidEnd) {
            this.announceNotRaidable(faction);
        }
    }
}
